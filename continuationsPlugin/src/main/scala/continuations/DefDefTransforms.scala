package continuations

import continuations.DefDefTransforms.*
import dotty.tools.dotc.ast.{tpd, TreeTypeMap, Trees}
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Annotations.ConcreteAnnotation
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Symbols, Types}
import dotty.tools.dotc.core.NullOpsDecorator.stripNull
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.{termName, typeName}
import dotty.tools.dotc.core.NameOps._
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType, Type}
import dotty.tools.dotc.report

import scala.annotation.tailrec
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

object DefDefTransforms extends TreesChecks:

  private def generateCompletion(owner: Symbol, returnType: Type)(using Context): Symbol =
    newSymbol(
      owner,
      Names.termName("completion"),
      Flags.LocalParam | Flags.SyntheticParam,
      requiredPackage("continuations")
        .requiredType("Continuation")
        .typeRef
        .appliedTo(returnType)
    )

  private def params(tree: tpd.DefDef, completionSym: Symbol)(
      using Context): List[tpd.ParamClause] =
    val suspendClazz = requiredClass(suspendFullName)
    val completion: tpd.ValDef = tpd.ValDef(completionSym.asTerm, theEmptyTree)

    if (tree.paramss.isEmpty) {
      List(List(completion))
    } else
      tree
        .paramss
        .zipWithIndex
        .map { (pc, i) =>
          val newPc: tpd.ParamClause = pc
            .filterNot {
              case p: Trees.ValDef[Type] =>
                p.typeOpt
                  .hasClassSymbol(suspendClazz) && p.symbol.flags.isOneOf(Flags.GivenOrImplicit)
              case t: Trees.TypeDef[Type] =>
                t.typeOpt
                  .hasClassSymbol(suspendClazz) && t.symbol.flags.isOneOf(Flags.GivenOrImplicit)
            }
            .asInstanceOf[tpd.ParamClause]
          if (i == 0) {
            newPc.appended(completion).asInstanceOf[tpd.ParamClause]
          } else newPc
        }
        .filterNot(_.isEmpty)

  /*
   * It works with only one top level `suspendContinuation` that just calls `resume`
   * and it replaces the parent method body keeping any rows before the `suspendContinuation` (but not ones after).
   */
  private def transformSuspendOneContinuationResume(tree: tpd.DefDef, resumeArg: tpd.Tree)(
      using Context): tpd.DefDef =
    val continuationTraitSym: ClassSymbol =
      requiredClass("continuations.Continuation")
    val continuationObjectSym: Symbol =
      continuationTraitSym.companionModule
    val safeContinuationClassSym: ClassSymbol =
      requiredClass("continuations.SafeContinuation")
    val interceptedMethodSym: TermSymbol =
      requiredPackage("continuations.intrinsics").requiredMethod("intercepted")

    val parent: Symbol = tree.symbol
    val returnType: Type =
      tree
        .rhs
        .find {
          case Trees.Inlined(fun, _, _) => fun.denot.matches(suspendContinuationMethod.symbol)
          case _ => false
        }
        .map(_.tpe)
        .get

    val rowsBeforeSuspend =
      tree
        .rhs
        .toList
        .flatMap {
          case Trees.Block(trees, tree) => trees :+ tree
          case tree => List(tree)
        }
        .takeWhile {
          case Trees.Inlined(call, _, _) =>
            !call.denot.matches(suspendContinuationMethod.symbol)
          case _ => true
        }

    val continuationTyped: Type =
      continuationTraitSym.typeRef.appliedTo(returnType)

    val completion = generateCompletion(parent, returnType)

    val continuation1: tpd.ValDef =
      tpd.ValDef(
        sym = newSymbol(parent, termName("continuation1"), Flags.Local, continuationTyped),
        rhs = ref(completion))

    val undecidedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Undecided"))

    val interceptedCall =
      ref(interceptedMethodSym)
        .appliedToType(returnType)
        .appliedTo(ref(continuation1.symbol))
        .appliedToNone

    val safeContinuationConstructor =
      tpd
        .New(ref(safeContinuationClassSym))
        .select(nme.CONSTRUCTOR)
        .appliedToType(returnType)
        .appliedTo(interceptedCall, undecidedState)

    val safeContinuation: tpd.ValDef =
      tpd.ValDef(
        newSymbol(
          parent,
          termName("safeContinuation"),
          Flags.Local,
          safeContinuationConstructor.tpe),
        safeContinuationConstructor)

    val safeContinuationRef =
      ref(safeContinuation.symbol)

    val suspendContinuationResume =
      safeContinuationRef.select(termName("resume")).appliedTo(resumeArg)

    val suspendContinuationGetThrow =
      safeContinuationRef.select(termName("getOrThrow")).appliedToNone

    val body = tpd.Block(
      rowsBeforeSuspend ++
        (continuation1 :: safeContinuation :: suspendContinuationResume :: Nil),
      suspendContinuationGetThrow
    )

    val suspendedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Suspended"))

    val finalMethodReturnType: tpd.TypeTree =
      tpd.TypeTree(
        OrType(
          OrType(ctx.definitions.AnyType, ctx.definitions.NullType, soft = false),
          suspendedState.symbol.namedType,
          soft = false)
      )

    cpy.DefDef(tree)(
      paramss = params(tree, completion),
      tpt = finalMethodReturnType,
      rhs = body
    )

  private def transformContinuationWithSuspend(tree: tpd.DefDef)(using Context): tpd.Tree =
    val newTree =
      tree match
        case HasSuspensionWithDependency(_) | HasSuspensionNotInReturnedValue(_) =>
          transformSuspendingStateMachine(tree)
        case CallsContinuationResumeWith(resumeArg) =>
          transformSuspendOneContinuationResume(tree, resumeArg)
        case BodyHasSuspensionPoint(_) =>
          cpy.DefDef(tree)() // any suspension that still needs a transformation
        case _ => transformNonSuspending(tree, cpy)

    report.logWith(s"new tree:")(newTree)

  def transformSuspendContinuation(tree: tpd.DefDef)(using Context): tpd.Tree =
    tree match
      case ReturnsContextFunctionWithSuspendType(_) | HasSuspendParameter(_) =>
        transformContinuationWithSuspend(tree)
      case _ => report.logWith(s"oldTree:")(tree)

  private def transformNonSuspending(tree: tpd.DefDef, cpy: TypedTreeCopier)(
      using ctx: Context): tpd.DefDef =
    val returnType = Types.OrType(tree.tpt.tpe, ctx.definitions.AnyType, false)
    val completion = generateCompletion(tree.symbol, returnType)
    cpy.DefDef(tree)(
      paramss = params(tree, completion),
      tpt = tpd.TypeTree(ctx.definitions.ObjectClass.typeRef))

  def countContinuationSynthetics(defdef: tpd.DefDef, oldCount: Int)(using Context): Int =
    defdef match
      case ReturnsContextFunctionWithSuspendType(_) | HasSuspendParameter(_) =>
        defdef match
          // need to make sure it calls continuation.resume?? see CallsContinuationResumeWith
          case HasSuspensionWithDependency(_) =>
            SuspensionPoints.unapplySeq(defdef).fold(0)(_.size + 6 + oldCount)
          case _ => oldCount
      case _ => oldCount

  @tailrec
  def createSyntheticNames(
      oldCounter: Int,
      suspensionPoints: List[tpd.Tree],
      counter: Int,
      names: List[String],
      label: String = "label"): List[String] =
    suspensionPoints match
      case _ :: Nil =>
        // without this it loops forever in counter != oldCounter
        s"$label$$$counter" :: names
      case tree :: remaining =>
        createSyntheticNames(oldCounter, remaining, counter - 1, s"$label$$$counter" :: names)
      case Nil if counter - oldCounter == oldCounter + 5 =>
        createSyntheticNames(oldCounter, Nil, counter - 1, s"${label}result$$$counter" :: names)
      case Nil if counter - oldCounter == oldCounter + 4 =>
        createSyntheticNames(oldCounter, Nil, counter - 1, s"result$$${counter}" :: names)
      case Nil if counter != oldCounter =>
        createSyntheticNames(
          oldCounter,
          suspensionPoints,
          counter - 1,
          s"continuation$$${counter}" :: names)
      case Nil =>
        names

  private def transformSuspendingStateMachine(tree: tpd.DefDef)(using ctx: Context) =
    SuspensionPoints.unapplySeq(report.logWith("tree has no suspension points:")(tree)) match
      case None | Some(Nil) => tree
      case Some(suspensionPoint :: Nil) =>
        val continuationClassRef = requiredClassRef(continuationFullName)
        val continuationModule = requiredModule(continuationFullName)
        val safeContinuationClass = requiredClass("continuations.SafeContinuation")
        val interceptedMethod =
          requiredPackage("continuations.intrinsics").requiredMethod("intercepted")
        val continuationImplClass = requiredClass("continuations.jvm.internal.ContinuationImpl")

        val parent = tree.symbol
        val treeOwner = parent.owner
        val defName = parent.name
        val returnType = tree.tpt.tpe

        val suspendedState =
          ref(continuationModule).select(termName("State")).select(termName("Suspended"))
        val newReturnType =
          tpd.TypeTree(
            Types.OrType(Types.OrNull(returnType), suspendedState.symbol.namedType, false)
          )

        val integerOR =
          defn.IntClass.requiredMethod(nme.OR, List(defn.IntType))
        val integerAND =
          defn.IntClass.requiredMethod(nme.AND, List(defn.IntType))
        val integerNE =
          defn.IntClass.requiredMethod(nme.NE, List(defn.IntType))
        val integerMin =
          ref(requiredModuleRef("scala.Int").select(Names.termName("MinValue")).symbol)

        val intType = defn.IntType
        val anyType = defn.AnyType
        val anyOrNullType = Types.OrNull(anyType)
        val anyNullSuspendedType =
          Types.OrType(Types.OrNull(defn.AnyType), suspendedState.symbol.namedType, false)

        val initialCounter =
          ctx.store(ctx.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get)
        val stateMachineContinuationClassName =
          s"${ctx.owner.name.show}$$$defName$$$initialCounter"
        val names = createSyntheticNames(
          ctx.store(
            ctx.property(ContinuationsPhase.continuationsPhaseOldCounterPropertyKey).get),
          List(suspensionPoint),
          initialCounter - 1,
          Nil
        )
        val continuationNames = names.takeWhile(_.startsWith("continuation"))
        val resultName = names.slice(continuationNames.size, continuationNames.size + 1)
        val labels = names.drop(continuationNames.size + 1)

        // class compileFromString-55580126-00e0-477a-8040-f0c76732df77.$package$foo$1
        val continuationsStateMachineSymbol = newCompleteClassSymbol(
          treeOwner,
          typeName(stateMachineContinuationClassName),
          SyntheticArtifact,
          List(continuationImplClass.typeRef),
          Scopes.newScope
        ).entered.asClass

        val continuationsStateMachineConstructorMethodCompletionParamName =
          termName("$completion")
        val continuationsStateMachineResultName = termName("$result")
        val continuationsStateMachineLabelParam = termName("$label")

        val continuationsStateMachineConstructorMethodSymbol = newConstructor(
          continuationsStateMachineSymbol,
          Flags.Synthetic,
          List(continuationsStateMachineConstructorMethodCompletionParamName),
          List(continuationClassRef.appliedTo(anyOrNullType))
        ).entered.asTerm

        val continuationsStateMachineConstructor =
          tpd.DefDef(continuationsStateMachineConstructorMethodSymbol)

        val eitherThrowableAnyNullSuspendedType =
          requiredClassRef("scala.util.Either").appliedTo(
            defn.ThrowableType,
            anyNullSuspendedType
          )
        val continuationStateMachineResult = tpd.ValDef(
          newSymbol(
            continuationsStateMachineSymbol,
            continuationsStateMachineResultName,
            Flags.Synthetic | Flags.Mutable,
            eitherThrowableAnyNullSuspendedType).entered.asTerm,
          Underscore(eitherThrowableAnyNullSuspendedType)
        )

        val continuationStateMachineLabel = tpd.ValDef(
          newSymbol(
            continuationsStateMachineSymbol,
            continuationsStateMachineLabelParam,
            Flags.Synthetic | Flags.Mutable,
            intType).entered.asTerm,
          Underscore(intType)
        )

        val invokeSuspendSymbol =
          newSymbol(
            continuationsStateMachineSymbol,
            Names.termName("invokeSuspend"),
            Flags.Override | Flags.Protected | Flags.Method,
            MethodType(
              List(termName("result")),
              List(eitherThrowableAnyNullSuspendedType),
              anyOrNullType
            )
          ).entered.asTerm

        val continuationsStateMachineThis: tpd.This =
          tpd.This(continuationsStateMachineSymbol)

        val continuationsStateMachineLabelSelect =
          continuationsStateMachineThis.select(continuationsStateMachineLabelParam)

        val completion = generateCompletion(parent, returnType)

        val transformedMethodParams = params(tree, completion)

        val transformedMethodSymbol =
          newSymbol(
            treeOwner,
            tree.name,
            parent.flags,
            MethodType(
              transformedMethodParams.flatMap {
                _.map {
                  case p: tpd.ValDef => p.name
                  case t: tpd.TypeDef => t.name.toTermName
                }
              },
              transformedMethodParams.flatMap {
                _.map {
                  case p: tpd.ValDef => p.tpt.tpe
                  case t: tpd.TypeDef => t.tpe
                }
              },
              newReturnType.tpe
            ),
            parent.privateWithin,
            parent.coord
          )

        treeOwner.unforcedDecls.openForMutations.replace(parent, transformedMethodSymbol)

        val transformedMethod: tpd.DefDef =
          tpd.DefDef(
            sym = transformedMethodSymbol,
            rhs = tree.rhs
          )

        val transformedMethodCompletionParam = ref(
          transformedMethod
            .termParamss
            .flatten
            .find(_.name.toString == "completion")
            .head
            .symbol)

        // override protected def invokeSuspend
        val invokeSuspendMethod = tpd.DefDef(
          invokeSuspendSymbol,
          tpd.Block(
            List(
              tpd.Assign(
                continuationsStateMachineThis.select(continuationsStateMachineResultName),
                ref(invokeSuspendSymbol.paramSymss.head.head)),
              tpd.Assign(
                continuationsStateMachineLabelSelect,
                continuationsStateMachineLabelSelect.select(integerOR).appliedTo(integerMin)
              )
            ),
            ref(transformedMethod.symbol).appliedTo(
              continuationsStateMachineThis
                .select(nme.asInstanceOf_)
                .appliedToType(continuationClassRef.appliedTo(returnType))
            )
          )
        )

        val $completion = continuationsStateMachineConstructor.paramss.flatten.head.symbol

        val continuationStateMachineResultSetter =
          newSymbol(
            continuationsStateMachineSymbol,
            continuationStateMachineResult.symbol.asTerm.name.setterName,
            Method | Flags.Accessor,
            info = MethodType(
              continuationStateMachineResult.symbol.asTerm.info.widenExpr :: Nil,
              defn.UnitType)
          ).entered.asTerm

        val continuationStateMachineLabelSetter =
          newSymbol(
            continuationsStateMachineSymbol,
            continuationStateMachineLabel.symbol.asTerm.name.setterName,
            Method | Flags.Accessor,
            info = MethodType(
              continuationStateMachineLabel.symbol.asTerm.info.widenExpr :: Nil,
              defn.UnitType)
          ).entered.asTerm

        val continuationStateMachineClass = ClassDefWithParents(
          continuationsStateMachineSymbol,
          continuationsStateMachineConstructor,
          List(
            tpd
              .New(ref(continuationImplClass))
              .select(nme.CONSTRUCTOR)
              .appliedTo(
                ref($completion),
                ref($completion).select(termName("context"))
              )),
          List(
            continuationStateMachineResult,
            continuationStateMachineLabel,
            tpd.DefDef(continuationStateMachineResultSetter, tpd.unitLiteral),
            tpd.DefDef(continuationStateMachineLabelSetter, tpd.unitLiteral),
            invokeSuspendMethod
          )
        )

        // the existing (foo) method
        def transformSuspendTree(suspendTree: tpd.Tree, parent: Symbol) = {
          // var $continuation: Continuation[Any] | Null = null
          val $continuation = tpd.ValDef(
            newSymbol(
              parent,
              termName("$continuation"),
              Local | Mutable | Synthetic,
              OrType(continuationClassRef.appliedTo(anyType), defn.NullType, soft = false)),
            nullLiteral
          )

          val continuationAsStateMachineClass =
            ref($continuation.symbol)
              .select(nme.asInstanceOf_)
              .appliedToType(continuationStateMachineClass.tpe)

          // case continuations$foo$ : continuations$foo$1 ...
          val case11Param =
            newSymbol(parent, nme.x_0, Flags.Case | Flags.CaseAccessor, defn.AnyType)
          val $continuationLabel =
            continuationAsStateMachineClass.select(continuationsStateMachineLabelParam)
          val case11 = tpd.CaseDef(
            tpd.Bind(case11Param, tpd.EmptyTree),
            ref(case11Param)
              .select(nme.isInstanceOf_)
              .appliedToType(continuationStateMachineClass.tpe)
              .select(defn.Boolean_&&)
              .appliedTo(
                ref(case11Param)
                  .select(nme.asInstanceOf_)
                  .appliedToType(continuationStateMachineClass.tpe)
                  .select(continuationsStateMachineLabelParam)
                  .select(integerAND)
                  .appliedTo(integerMin)
                  .select(integerNE)
                  .appliedTo(tpd.Literal(Constant(0x0)))),
            tpd.Block(
              List(
                tpd.Assign(
                  ref($continuation.symbol),
                  ref(case11Param)
                    .select(nme.asInstanceOf_)
                    .appliedToType(continuationStateMachineClass.tpe))),
              tpd.Assign(
                $continuationLabel,
                $continuationLabel.select(defn.Int_-).appliedTo(integerMin))
            )
          )

          // case _ => ...
          val case12 = tpd.CaseDef(
            Underscore(anyType),
            tpd.EmptyTree,
            tpd.Assign(
              ref($continuation.symbol),
              tpd
                .New(tpd.TypeTree(continuationStateMachineClass.tpe))
                .select(nme.CONSTRUCTOR)
                .appliedTo(
                  transformedMethodCompletionParam
                    .select(nme.asInstanceOf_)
                    .appliedToType(
                      continuationClassRef.appliedTo(anyOrNullType)
                    ))
            )
          )

          // completion match {...}
          val completionMatch =
            tpd.Match(transformedMethodCompletionParam, List(case11, case12))

          // $val result
          val $result =
            tpd.ValDef(
              newSymbol(
                parent,
                termName("$result"),
                Local,
                eitherThrowableAnyNullSuspendedType),
              continuationAsStateMachineClass.select(continuationsStateMachineResultName)
            )

          // $result.fold(t => throw t, _ => ())
          val $resultFold = ref($result.symbol)
            .select(termName("fold"))
            .appliedToType(defn.UnitType)
            .appliedTo(
              tpd.Lambda(
                MethodType.apply(List(defn.ThrowableType))(_ => defn.NothingType),
                trees => tpd.Throw(trees.head)),
              tpd.Lambda(
                MethodType(List(eitherThrowableAnyNullSuspendedType))(_ => defn.UnitType),
                _ => unitLiteral)
            )

          // val safeContinuation ...
          val undecidedState =
            ref(continuationModule).select(termName("State")).select(termName("Undecided"))

          val suspendContinuationType = suspendTree.tpe
          val interceptedCall =
            ref(interceptedMethod)
              .appliedToType(suspendContinuationType)
              .appliedTo(ref($continuation.symbol))
              .appliedToNone

          val safeContinuationConstructor =
            tpd
              .New(ref(safeContinuationClass))
              .select(nme.CONSTRUCTOR)
              .appliedToType(suspendContinuationType)
              .appliedTo(interceptedCall, undecidedState)

          val safeContinuation: tpd.ValDef =
            tpd.ValDef(
              newSymbol(
                parent,
                termName("safeContinuation"),
                Flags.Local,
                safeContinuationConstructor.tpe),
              safeContinuationConstructor)

          val safeContinuationRef =
            ref(safeContinuation.symbol)

          val resumeArgs: List[tpd.Tree] =
            suspendTree.filterSubTrees(treeCallsResume).flatMap {
              case Trees.Apply(_, List(arg)) => Option(arg)
              case _ => Option.empty
            }

          val suspendContinuationResume =
            if (resumeArgs.isEmpty)
              // suspendTreeArgs
              suspendTree
                .filterSubTrees {
                  case Trees.DefDef(_, _, _, _) => true
                  case _ => false
                }
                .flatMap {
                  case d @ Trees.DefDef(_, _, _, _) => Option(d.rhs)
                  case _ => Option.empty
                }
                .headOption
                .getOrElse(tpd.EmptyTree)
            else if (resumeArgs.size == 1)
              safeContinuationRef.select(termName("resume")).appliedTo(resumeArgs.head)
            else
              report.error("NotSupported: Can't have more than 1 resume()")
              tpd.EmptyTree

          val suspendContinuationGetThrow =
            tpd.ValDef(
              newSymbol(parent, termName("orThrow"), Flags.Local, anyNullSuspendedType),
              safeContinuationRef.select(termName("getOrThrow")).appliedToNone
            )

          // if (orThrow == ...){}
          val ifOrThrowReturn =
            tpd.If(
              ref(suspendContinuationGetThrow.symbol)
                .select(nme.Equals)
                .appliedTo(suspendedState),
              tpd.Return(suspendedState, parent),
              tpd.EmptyTree
            )

          // case 0
          val case21 = tpd.CaseDef(
            tpd.Literal(Constant(0)),
            tpd.EmptyTree,
            tpd.Block(
              List(
                $resultFold,
                tpd.Assign(
                  continuationAsStateMachineClass.select(continuationsStateMachineLabelParam),
                  tpd.Literal(Constant(1))),
                safeContinuation,
                suspendContinuationResume,
                suspendContinuationGetThrow
              ),
              ifOrThrowReturn
            )
          )

          // case 1
          val case22 = tpd.CaseDef(
            tpd.Literal(Constant(1)),
            tpd.EmptyTree,
            $resultFold
          )

          // case _
          // like defn.ClassCastExceptionClass_stringConstructor
          val IllegalArgumentExceptionClass_stringConstructor: TermSymbol =
            defn
              .IllegalArgumentExceptionClass
              .info
              .member(nme.CONSTRUCTOR)
              .suchThat(_.info.firstParamTypes match {
                case List(pt) =>
                  pt.stripNull.isRef(defn.StringClass)
                case _ => false
              })
              .symbol
              .asTerm

          val case23 = tpd.CaseDef(
            tpd.Underscore(intType),
            tpd.EmptyTree,
            tpd.Throw(
              tpd.New(
                defn.IllegalArgumentExceptionType,
                IllegalArgumentExceptionClass_stringConstructor,
                List(tpd.Literal(Constant("call to 'resume' before 'invoke' with coroutine")))
              )
            )
          )

          // $label match {...}
          val labelMatch = tpd
            .Match(
              continuationAsStateMachineClass.select(continuationsStateMachineLabelParam),
              List(case21, case22, case23)
            )
            .withType(anyNullSuspendedType)

          tpd.Block(List($continuation, completionMatch, $result), labelMatch)
        }

        val substituteContinuation = new TreeTypeMap(
          treeMap = {
            case tree @ Trees.Inlined(call, _, _) if treeCallsSuspend(tree) =>
              transformSuspendTree(call, transformedMethod.symbol)
            case tree => tree
          }
        )

        // TODO: delete, only for testing
        val run = ref(transformedMethod.symbol)
          .appliedTo(ref(requiredModule("continuations.Continuation").requiredMethod("cont")))

        val transformedTree =
          tpd.Thicket(
            continuationStateMachineClass ::
              cpy.DefDef(transformedMethod)(rhs =
                substituteContinuation.transform(tree.rhs)) :: run :: Nil)

//        println(transformedTree.show)

        println(s"state machine and new defdef names: $names")
        println(s"state machine and new defdef continuationNames: $continuationNames")
        println(s"state machine and new defdef resultName: $resultName")
        println(s"state machine and new defdef labels: $labels")
        report.logWith("state machine and new defdef:")(transformedTree)

      case Some(_ :: rest) =>
        report.error("NotSupported: Can't have more that 1 suspendContinuation in the method")
        tree
