package continuations

import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.StdNames.nme.CONSTRUCTOR
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.*
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.report
import scala.annotation.tailrec
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types.TypeRef
import dotty.tools.dotc.core.Types.NoType
import dotty.tools.dotc.core.Scopes
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Types
import scala.collection.mutable.ListBuffer
import dotty.tools.dotc.core.Annotations.ConcreteAnnotation
import org.scalajs.ir.Trees.Return.apply
import org.scalajs.ir.Trees.LabelIdent.apply
import org.scalajs.ir.Trees.Block.apply
import scala.Symbol.apply
import dotty.tools.dotc.ast.Trees.Block

object DefDefTransforms:

  private def generateCompletion(owner: Symbol, returnType: Type)(using Context): Symbol =
    newSymbol(
      owner,
      termName("completion"),
      LocalParam,
      requiredPackage("continuations")
        .requiredType("Continuation")
        .typeRef
        .appliedTo(returnType)
    )

  private def params(tree: DefDef, completionSym: Symbol)(using Context): List[ParamClause] =
    val suspendClazz = requiredClass(suspendFullName)
    val completion: ValDef = ValDef(completionSym.asTerm, Trees.theEmptyTree)

    if (tree.paramss.isEmpty) {
      List(List(completion))
    } else
      tree
        .paramss
        .zipWithIndex
        .map { (pc, i) =>
          val newPc: ParamClause = pc
            .filterNot {
              case p: Trees.ValDef[Type] =>
                p.typeOpt
                  .hasClassSymbol(suspendClazz) && p.symbol.flags.isOneOf(GivenOrImplicit)
              case t: Trees.TypeDef[Type] =>
                t.typeOpt
                  .hasClassSymbol(suspendClazz) && t.symbol.flags.isOneOf(GivenOrImplicit)
            }
            .asInstanceOf[ParamClause]
          if (i == 0) {
            newPc.appended(completion).asInstanceOf[ParamClause]
          } else newPc
        }
        .filterNot(_.isEmpty)

  /**
   * For each suspended defdef with dependent calculations, there will need to be 1 state
   * machine class, a result variable, an initial continuation, a suspension continuation, a
   * result continuation, and a result label for each suspension continuation. So N + 7 +
   * oldCount.
   *
   * @param defdef
   *   The [[dotty.tools.dotc.ast.tpd.DefDef]] to transform
   * @param oldCount
   *   The value of the previous counter.
   * @return
   *   The number of suspension points + 7 + the oldCount.
   */
  def countContinuationSynthetics(defdef: DefDef, oldCount: Int)(using Context): Int =
    defdef match
      case ReturnsContextFunctionWithSuspendType(_) | HasSuspendParameter(_) =>
        defdef match
          case HasSuspensionWithDependency(_) =>
            SuspensionPoints.unapplySeq(defdef).fold(0)(_.size + 7 + oldCount)
          case _ => oldCount
      case _ => oldCount

  /**
   * Transforms suspended functions into a continuations state machine.
   *
   * @param tree
   *   The tree to potentially suspend.
   * @return
   *   If the tree is not a suspending DefDef, then the tree is returned as is. If the tree is a
   *   suspended DefDef, then a continuation taking te return type of the suspended DefDef is
   *   added as a synthetic argument to the DefDef. The body is translated into a state machine.
   *   A single continuation is a simple throw or get. Any dependent calculations on suspended
   *   continuations within the defdef body create return labels that are jumped to as the
   *   continuation transitions from suspended to completed states.
   */
  def transformSuspendContinuation(tree: DefDef)(using Context): Tree =
    tree match
      case ReturnsContextFunctionWithSuspendType(_) | HasSuspendParameter(_) =>
        report.logWith(s"transformed tree: ")(transformContinuationWithSuspend(tree))
      case _ => report.logWith(s"oldTree:")(tree)
  /*
   * It works with only one top level `suspendContinuation` that just calls `resume`
   * and it replaces the parent method body keeping any rows before the `suspendContinuation` (but not ones after).
   */
  private def transformSuspendNoParametersOneContinuationResume(tree: DefDef, resumeArg: Tree)(
      using Context): DefDef =
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
          case Trees.Inlined(fun, _, _) =>
            fun.symbol.showFullName == suspendContinuationFullName
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
            call.symbol.showFullName != suspendContinuationFullName
          case _ => true
        }

    val continuationTyped: Type =
      continuationTraitSym.typeRef.appliedTo(returnType)

    val completion = generateCompletion(parent, returnType)

    val continuation1: ValDef =
      ValDef(
        sym = newSymbol(parent, termName("continuation1"), Local, continuationTyped),
        rhs = ref(completion))

    val undecidedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Undecided"))

    val interceptedCall =
      ref(interceptedMethodSym)
        .appliedToType(returnType)
        .appliedTo(ref(continuation1.symbol))
        .appliedToNone

    val safeContinuationConstructor =
      New(ref(safeContinuationClassSym))
        .select(CONSTRUCTOR)
        .appliedToType(returnType)
        .appliedTo(interceptedCall, undecidedState)

    val safeContinuation: ValDef =
      ValDef(
        newSymbol(parent, termName("safeContinuation"), Local, safeContinuationConstructor.tpe),
        safeContinuationConstructor)

    val safeContinuationRef =
      ref(safeContinuation.symbol)

    val suspendContinuation: ValDef =
      ValDef(
        newSymbol(parent, termName("suspendContinuation"), Local, ctx.definitions.IntType),
        Literal(Constant(0))
      )

    val suspendContinuationResume =
      safeContinuationRef.select(termName("resume")).appliedTo(resumeArg)

    val suspendContinuationGetThrow =
      safeContinuationRef.select(termName("getOrThrow")).appliedToNone

    val body = tpd.Block(
      rowsBeforeSuspend ++
        (continuation1 :: safeContinuation :: suspendContinuation :: suspendContinuationResume :: Nil),
      suspendContinuationGetThrow
    )

    val suspendedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Suspended"))

    val finalMethodReturnType: TypeTree =
      TypeTree(
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

  private def transformSuspendOneContinuationResume(tree: DefDef, resumeArg: Tree)(
      using Context): DefDef =
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
          case Trees.Inlined(fun, _, _) =>
            fun.symbol.showFullName == suspendContinuationFullName
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
            call.symbol.showFullName != suspendContinuationFullName
          case _ => true
        }

    val continuationTyped: Type =
      continuationTraitSym.typeRef.appliedTo(returnType)

    val completion =
      newSymbol(parent, termName("completion"), LocalParam, continuationTyped)

    val continuation1: ValDef =
      ValDef(
        newSymbol(parent, termName("continuation1"), Local, continuationTyped),
        ref(completion))

    val undecidedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Undecided"))

    val interceptedCall =
      ref(interceptedMethodSym)
        .appliedToType(returnType)
        .appliedTo(ref(continuation1.symbol))
        .appliedToNone

    val safeContinuationConstructor =
      New(ref(safeContinuationClassSym))
        .select(CONSTRUCTOR)
        .appliedToType(returnType)
        .appliedTo(interceptedCall, undecidedState)

    val safeContinuation: ValDef =
      ValDef(
        newSymbol(parent, termName("safeContinuation"), Local, safeContinuationConstructor.tpe),
        safeContinuationConstructor)

    val safeContinuationRef =
      ref(safeContinuation.symbol)

    val suspendContinuation: ValDef =
      ValDef(
        newSymbol(parent, termName("suspendContinuation"), Local, ctx.definitions.IntType),
        Literal(Constant(0))
      )

    val suspendContinuationResume =
      safeContinuationRef.select(termName("resume")).appliedTo(resumeArg)

    val suspendContinuationGetThrow =
      safeContinuationRef.select(termName("getOrThrow")).appliedToNone

    val body = tpd.Block(
      rowsBeforeSuspend ++
        (continuation1 :: safeContinuation :: suspendContinuation :: suspendContinuationResume :: Nil),
      suspendContinuationGetThrow
    )

    val suspendedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Suspended"))

    val finalMethodReturnType: TypeTree =
      TypeTree(
        OrType(
          OrType(ctx.definitions.AnyType, ctx.definitions.NullType, soft = false),
          suspendedState.symbol.namedType,
          soft = false)
      )

    DefDef(parent.asTerm, List(List(completion)), finalMethodReturnType.tpe, body)

  @tailrec
  def createSyntheticNames(
      oldCounter: Int,
      suspensionPoints: List[Tree],
      counter: Int,
      names: List[String]): List[String] =
    suspensionPoints match
      case tree :: remaining =>
        createSyntheticNames(oldCounter, remaining, counter - 1, s"$label$$$counter" :: names)
      case Nil
          if counter - oldCounter >= oldCounter + 5 && counter - oldCounter >= oldCounter + 6 =>
        createSyntheticNames(oldCounter, Nil, counter - 1, s"${label}result$$$counter" :: names)
      case Nil if counter - oldCounter == oldCounter + 4 =>
        createSyntheticNames(oldCounter, Nil, counter - 1, s"result$$${counter}" :: names)
      case Nil if counter != oldCounter =>
        createSyntheticNames(
          oldCounter,
          suspensionPoints,
          counter - 1,
          s"continuation$$${counter}" :: names)
      case Nil => names

  private def transformSuspendingStateMachine(tree: DefDef)(using ctx: Context) =
    SuspensionPoints
      .unapplySeq(report.logWith("tree has no suspension points:")(tree))
      .fold(tree) { suspensionPoints =>
        val initialCounter =
          ctx.store(ctx.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get)
        val stateMachineContinuationClassName =
          s"${ctx.owner.name.show}Continuation$$${initialCounter}"

        val names = createSyntheticNames(
          ctx.store(
            ctx.property(ContinuationsPhase.continuationsPhaseOldCounterPropertyKey).get),
          suspensionPoints,
          initialCounter - 1,
          Nil
        )
        val continuationNames = names.takeWhile(_.startsWith(continuation))
        val resultName = names.drop(continuationNames.size).take(1).head
        val labels = names.drop(continuationNames.size + 1)
        val continuationsStateMachineScope = Scopes.newScope
        val treeOwner = tree.symbol.owner
        val baseContinuationImplClazz =
          requiredClass("continuations.jvm.internal.BaseContinuationImpl")
        val continuationsStateMachineSymbol = Symbols.newCompleteClassSymbol(
          treeOwner,
          Names.typeName(stateMachineContinuationClassName),
          SyntheticArtifact,
          List(baseContinuationImplClazz.typeRef),
          continuationsStateMachineScope,
          NoType,
          treeOwner
        )
        val intType = ctx.definitions.IntType
        val continuationsStateMachineConstructorMethodInputParamName = Names.termName("$input")
        val memberInputSym = continuationsStateMachineScope.enter(
          continuationsStateMachineConstructorMethodInputParamName,
          Symbols.newSymbol(
            continuationsStateMachineSymbol,
            continuationsStateMachineConstructorMethodInputParamName,
            Mutable | Deferred,
            intType)
        )
        val continuationsStateMachineConstructorMethodLabelParamName = Names.termName("$label")
        val memberLabelSym = continuationsStateMachineScope.enter(
          continuationsStateMachineConstructorMethodLabelParamName,
          Symbols.newSymbol(
            continuationsStateMachineSymbol,
            continuationsStateMachineConstructorMethodLabelParamName,
            Mutable | Deferred,
            intType)
        )
        val continuationIntTpe = requiredClassRef(continuationFullName).appliedTo(intType)
        val continuationsStateMachineConstructorMethodCompletionParamName =
          Names.termName("$completion")
        val memberCompletionSym = continuationsStateMachineScope.enter(
          continuationsStateMachineConstructorMethodCompletionParamName,
          Symbols.newSymbol(
            continuationsStateMachineSymbol,
            continuationsStateMachineConstructorMethodCompletionParamName,
            Mutable | Deferred,
            continuationIntTpe)
        )
        val continuationsStateMachineConstructorMethodSymbol = Symbols.newConstructor(
          continuationsStateMachineSymbol,
          Flags.Synthetic,
          List(
            continuationsStateMachineConstructorMethodCompletionParamName,
            continuationsStateMachineConstructorMethodInputParamName,
            continuationsStateMachineConstructorMethodLabelParamName
          ),
          List(continuationIntTpe, intType, intType)
        )
        val continutationsStateMachineConstructor =
          DefDef(
            continuationsStateMachineConstructorMethodSymbol,
            (paramms: List[List[Tree]]) =>
              tpd.Block(
                List(
                  Assign(
                    This(continuationsStateMachineSymbol).select(
                      continuationsStateMachineConstructorMethodCompletionParamName),
                    paramms.head(0)),
                  Assign(
                    This(continuationsStateMachineSymbol).select(
                      continuationsStateMachineConstructorMethodInputParamName),
                    paramms.head(1)),
                  Assign(
                    This(continuationsStateMachineSymbol).select(
                      continuationsStateMachineConstructorMethodLabelParamName),
                    paramms.head(1))
                ),
                Literal(Constant(()))
              )
          )
        val continutationsStateMachineResultName = Names.termName("$result")
        val anyNullSuspendedType = Types.OrType(
          Types.OrNull(ctx.definitions.AnyType),
          requiredModuleRef("continuations.Continuation.State.Suspended"),
          false)
        val eitherThrowableAnyNullSuspendedType =
          requiredClassRef("scala.util.Either").appliedTo(
            ctx.definitions.ThrowableType,
            anyNullSuspendedType
          )
        val continuationStateMachineResult = ValDef(
          continuationsStateMachineScope.enter(
            Symbols.newSymbol(
              continuationsStateMachineSymbol,
              continutationsStateMachineResultName,
              Synthetic | Mutable,
              eitherThrowableAnyNullSuspendedType)),
          Underscore(eitherThrowableAnyNullSuspendedType)
        )
        val anyOrNullType = OrNull(ctx.definitions.AnyType)
        // sym: TermSymbol, paramss: List[List[Symbol]], resultType: Type, rhs: Tree
        val invokeSuspendSymbol = continuationsStateMachineScope.enter(
          Symbols.newSymbol(
            continuationsStateMachineSymbol,
            Names.termName("invokeSuspend"),
            Override | Method,
            anyOrNullType))
        val invokeSuspendResultParam = ValDef(
          Symbols.newSymbol(
            invokeSuspendSymbol,
            Names.termName("result"),
            LocalParam,
            eitherThrowableAnyNullSuspendedType),
          EmptyTree)
        val invokeSuspendParmss = List(List(invokeSuspendResultParam))
        val continuationsStateMachineThisSymbol =
          ref(This(continuationsStateMachineSymbol).symbol)
        val continuationsStateMachineLabelSelect = continuationsStateMachineThisSymbol.select(
          continuationsStateMachineConstructorMethodLabelParamName)

        val invokeSuspendMethod = DefDef(
          invokeSuspendSymbol.asTerm,
          invokeSuspendParmss.map(_.map(_.symbol)),
          anyOrNullType,
          tpd.Block(
            List(
              Assign(
                continuationsStateMachineThisSymbol.select(
                  continutationsStateMachineResultName),
                ref(invokeSuspendResultParam.symbol)),
              Assign(
                continuationsStateMachineLabelSelect,
                continuationsStateMachineLabelSelect
                  .select(
                    ctx
                      .definitions
                      .IntClass
                      .requiredMethod(nme.OR, List(ctx.definitions.IntType)))
                  .appliedTo(
                    ref(requiredModuleRef("scala.Int").select(Names.termName("MinValue"))))
              )
            ), {
              /**
                * So, as you can see here, in this overridden method
                * tree for invokeSuspend, the return item is an
                * application of the suspended method we are
                * transforming, which hasn't yet been replaced with
                * the TreeCopier. Even specifying that the transformed
                * paramss must contain additional specific
                * Continuation[Int] parameter via `disambiguate` will
                * result in the following compilation error:
                * ```
                  wrong
                  number of arguments at continuations for (x:
                  Int)(using x$2: continuations.Suspend): Int:
                  (continuations.compileFromString-14cb32b5-3515-4b7a-8e72-84355aa5b6c9.$package.foo5
                  : (x: Int)(using x$2: continuations.Suspend): Int),
                  expected: 1, found: 2
                * ```
                * .
                * Our proposed solution is to enter the method symbol
                * into the owning scope for the transformed tree in
                * `prepareForDefDef` in the plugin phase, then to
                * replace the copied tree, and replace its symbol with
                * the new symbol already present in the context and
                * owning scope. This sholud allow the overridden
                * method transform referencing the transformed defdef
                * to compile.
                */
              println("before bad application")
              val x = ref(
                tree
                  .denot
                  .disambiguate(s => s.info.firstParamTypes.contains(continuationIntTpe))
                  .asSymDenotation
                  .currentSymbol).appliedTo(
                Literal(Constant(0)),
                continuationsStateMachineThisSymbol
                  .select(Names.termName("asInstanceOf"))
                  .appliedToType(
                    requiredClassRef(continuationFullName).appliedTo(ctx.definitions.IntType))
              )
              println("after bad application")
              x
            }
          )
        )
        val continuationStateMachineClass = ClassDefWithParents(
          continuationsStateMachineSymbol,
          continutationsStateMachineConstructor,
          List(
            ref(baseContinuationImplClazz.primaryConstructor).appliedTo(
              ref(
                baseContinuationImplClazz
                  .primaryConstructor
                  .paramSymss
                  .head
                  .filter(_.asTerm.name.show == "completion")
                  .head))),
          List(continuationStateMachineResult)
        )
        val defDefCompletionParam: ValDef = ValDef(
          Symbols.newSymbol(
            tree.symbol,
            Names.termName("$completion"),
            LocalParam | SyntheticParam,
            requiredClassRef(continuationFullName).appliedTo(ctx.definitions.IntType)),
          EmptyTree
        )
        val continuationOfAny =
          requiredClassRef(continuationFullName).appliedTo(ctx.definitions.AnyType)
        val labelBuffer = labels.to(ListBuffer)
        val continuationNamesBuffer = continuationNames.to(ListBuffer)
        val initialReturnLabelSymbol = Symbols.newSymbol(
          tree.symbol,
          Names.termName(labelBuffer.remove(0)),
          Local | Label,
          ctx.definitions.UnitType)
        val continuation1 = ValDef(
          Symbols.newSymbol(
            tree.symbol,
            Names.termName("$continuation"),
            Local | Mutable | Synthetic,
            continuationOfAny),
          Underscore(continuationOfAny))
        val firstContinuation = Symbols.newSymbol(
          tree.symbol,
          Names.termName(continuationNamesBuffer.remove(0)),
          Local | Mutable | Synthetic,
          continuationStateMachineClass.tpe)
        val continuation2 = Symbols.newSymbol(
          tree.symbol,
          Names.termName(continuationNamesBuffer.remove(0)),
          Local | Synthetic,
          firstContinuation.info)
        val resultSymbol = Symbols.newSymbol(
          tree.symbol,
          Names.termName("$result"),
          Local | Mutable | Synthetic,
          eitherThrowableAnyNullSuspendedType)
        val safeContinuationInt =
          requiredClassRef("continuations.SafeContinuation").appliedTo(ctx.definitions.IntType)
        val safeContinuationIntSym = Symbols.newSymbol(
          tree.symbol,
          Names.termName("$safeContinuation"),
          Local,
          safeContinuationInt)
        val continuation3Sym = Symbols.newSymbol(
          tree.symbol,
          Names.termName(continuationNamesBuffer.remove(0)),
          Local | Synthetic,
          safeContinuationInt)
        val oSym = Symbols.newSymbol(
          tree.symbol,
          Names.termName("$o"),
          Synthetic | Local,
          OrType(anyOrNullType, requiredModule("Continuation.State.Suspended").info, false))
        val orThrowSym = Symbols.newSymbol(
          tree.symbol,
          Names.termName("$orThrow"),
          Local | Mutable | Synthetic,
          ctx.definitions.ObjectType)
        val zSym = Symbols.newSymbol(
          tree.symbol,
          Names.termName("$z"),
          Local | Mutable | Synthetic,
          ctx.definitions.IntType)
        val finalLabeledReturnSym = Symbols.newSymbol(
          tree.symbol,
          Names.termName(labelBuffer.remove(0)),
          Local | Label,
          ctx.definitions.IntType
        )
        val ySymbol = Symbols.newSymbol(
          tree.symbol,
          Names.termName("y"),
          Synthetic | Local,
          ctx.definitions.IntType)

        report.logWith("state machine and new defdef:")(
          Thicket(
            continuationStateMachineClass,
            cpy.DefDef(tree)(
              paramss = params(tree, defDefCompletionParam.symbol),
              tpt = TypeTree(anyOrNullType),
              tpd.Block(
                List(
                  continuation1,
                  ValDef(zSym.asTerm, Literal(Constant(0))),
                  If(
                    ref(defDefCompletionParam.symbol)
                      .select(Names.termName("isInstanceOf"))
                      .appliedToType(continuationStateMachineClass.tpe),
                    Return(
                      Typed(
                        unitLiteral,
                        TypeTree(
                          OrType(
                            anyOrNullType,
                            requiredModuleRef("continuations.Continuation.State.Suspended"),
                            false))),
                      initialReturnLabelSymbol),
                    EmptyTree
                  ), {
                    ValDef(
                      firstContinuation.asTerm,
                      ref(continuation1.symbol)
                        .select(Names.termName("asInstanceOf"))
                        .appliedToType(continuationStateMachineClass.tpe)
                    )
                  },
                  If(
                    ref(firstContinuation)
                      .select(Names.termName("$label"))
                      .select(ctx
                        .definitions
                        .IntClass
                        .requiredMethod(nme.AND, List(ctx.definitions.IntType)))
                      .appliedTo(
                        ref(requiredModuleRef("scala.Int")
                          .select(Names.termName("MinValue"))
                          .symbol)
                      )
                      .select(ctx
                        .definitions
                        .IntClass
                        .requiredMethod(nme.NE, List(ctx.definitions.IntType)))
                      .appliedTo(Literal(Constant(0x0))),
                    tpd.Block(
                      List(ValDef(continuation2.asTerm, ref(continuation1.symbol))),
                      Assign(
                        ref(continuation2.asTerm).select(Names.termName("$label")),
                        ref(continuation2.asTerm)
                          .select(Names.termName("$label"))
                          .select(ctx
                            .definitions
                            .IntClass
                            .requiredMethod(nme.MINUS, List(ctx.definitions.IntType)))
                          .appliedTo(
                            ref(requiredModuleRef("scala.Int")
                              .select(Names.termName("MinValue"))
                              .symbol)
                          )
                      )
                    ),
                    Return(
                      Typed(
                        unitLiteral,
                        TypeTree(
                          OrType(
                            anyOrNullType,
                            requiredModuleRef("continuations.Continuation.State.Suspended"),
                            false))),
                      initialReturnLabelSymbol)
                  )
                ),
                Labeled(
                  initialReturnLabelSymbol.asTerm,
                  Return(
                    tpd.Block(
                      List(
                        ValDef(
                          resultSymbol.asTerm,
                          ref(continuation1.symbol)
                            .select(Names.termName("asInstanceOf"))
                            .appliedToType(continuationStateMachineClass.tpe)
                            .select(Names.termName("$result"))
                        ),
                        ValDef(orThrowSym.asTerm, Underscore(ctx.definitions.ObjectType))
                      ),
                      Match(
                        Typed(
                          ref(continuation1.symbol)
                            .select(Names.termName("asInstanceOf"))
                            .appliedToType(continuationStateMachineClass.tpe)
                            .select(Names.termName("$label")),
                          TypeTree(
                            Types.AnnotatedType(
                              ref(continuation1.symbol).tpe,
                              ConcreteAnnotation(
                                New(requiredClassRef("scala.annotation.switch")))))
                        ),
                        List(
                          CaseDef(
                            Literal(Constant(0)),
                            EmptyTree,
                            tpd.Block(
                              List(
                                ref(resultSymbol)
                                  .select(
                                    Names.termName("fold")
                                  )
                                  .appliedToType(ctx.definitions.UnitType)
                                  .appliedTo(
                                    Lambda(
                                      MethodType.fromSymbols(
                                        List(
                                          Symbols.newSymbol(
                                            tree.symbol,
                                            Names.termName("t"),
                                            LocalParam,
                                            ctx.definitions.ThrowableType)),
                                        ctx.definitions.NothingType),
                                      args => Throw(args.head)
                                    ),
                                    Lambda(
                                      MethodType.fromSymbols(
                                        List(
                                          Symbols.newSymbol(
                                            tree.symbol,
                                            Names.termName("t"),
                                            LocalParam,
                                            OrType(
                                              anyOrNullType,
                                              requiredModuleRef(
                                                "continuations.Continuation.State.Suspended"),
                                              false))),
                                        ctx.definitions.UnitType
                                      ),
                                      _ => Literal(Constant(()))
                                    )
                                  ),
                                Assign(
                                  ref(continuation1.symbol)
                                    .select(Names.termName("asInstanceOf"))
                                    .appliedToTypes(List(continuationStateMachineClass.tpe))
                                    .select(Names.termName("$input")),
                                  ref(tree.paramss.head.filter(_.name.show == "x").head.symbol)
                                ),
                                Assign(
                                  ref(continuation1.symbol)
                                    .select(Names.termName("asInstanceOf"))
                                    .appliedToTypes(List(continuationStateMachineClass.tpe))
                                    .select(Names.termName("$label")),
                                  Literal(Constant(1))
                                ),
                                ValDef(
                                  safeContinuationIntSym.asTerm,
                                  New(
                                    safeContinuationInt,
                                    List(
                                      ref(
                                        requiredMethod("continuations.intrinsics.intercepted"))
                                        .appliedToType(ctx.definitions.IntType)
                                        .appliedTo(ref(defDefCompletionParam.symbol))
                                        .appliedToNone,
                                      ref(requiredModule(
                                        "continuations.Continuation.State.Undecided"))
                                    )
                                  )
                                ),
                                ValDef(continuation3Sym.asTerm, ref(safeContinuationIntSym)),
                                ref(continuation3Sym)
                                  .select(Names.termName("resume"))
                                  .appliedTo(ref(requiredModule("scala.util.Right"))
                                    .select(Names.termName("apply"))
                                    .appliedToTypes(List(
                                      ctx.definitions.ThrowableType,
                                      ctx.definitions.IntType))
                                    .appliedTo(ref(
                                      tree.paramss.head.filter(_.name.show == "x").head.symbol)
                                      .select(
                                        nme.Plus,
                                        s =>
                                          s.info
                                            .firstParamTypes
                                            .contains(ctx.definitions.IntType))
                                      .appliedTo(Literal(Constant(1))))),
                                ValDef(
                                  oSym.asTerm,
                                  ref(continuation3Sym.asTerm)
                                    .select(Names.termName("getOrThrow"))
                                    .appliedToNone),
                                Assign(ref(orThrowSym), ref(oSym))
                              ),
                              If(
                                ref(
                                  requiredModule("continuations.Continuation.State.Suspended"))
                                  .select(nme.Equals)
                                  .appliedTo(ref(oSym)),
                                Return(
                                  ref(requiredModule(
                                    "continuations.Continuation.State.Suspended")),
                                  tree.symbol),
                                EmptyTree
                              )
                            )
                          ),
                          CaseDef(
                            Literal(Constant(1)),
                            EmptyTree,
                            tpd.Block(
                              List(
                                Assign(
                                  ref(zSym.asTerm),
                                  ref(continuation1.symbol)
                                    .select(nme.asInstanceOf_)
                                    .appliedToType(continuationStateMachineClass.tpe)
                                    .select(Names.termName("$input"))
                                ),
                                ref(resultSymbol.asTerm)
                                  .select(Names.termName("fold"))
                                  .appliedToType(ctx.definitions.UnitType)
                                  .appliedToArgs(List(
                                    Lambda(
                                      MethodType(
                                        List(ctx.definitions.ThrowableType),
                                        ctx.definitions.NothingType),
                                      paramss => Throw(ref(paramss.head.symbol.asTerm))),
                                    Lambda(
                                      MethodType.apply(
                                        List(anyNullSuspendedType),
                                        ctx.definitions.UnitType),
                                      paramss => Literal(Constant(())))
                                  )),
                                Assign(ref(orThrowSym), ref(resultSymbol))
                              ),
                              Labeled(
                                finalLabeledReturnSym.asTerm,
                                Return(
                                  tpd.Block(
                                    List(
                                      ValDef(
                                        ySymbol.asTerm,
                                        ref(orThrowSym)
                                          .select(nme.asInstanceOf_)
                                          .appliedToType(ctx.definitions.IntType)
                                      )
                                    ),
                                    ref(zSym.asTerm)
                                      .select(
                                        nme.Plus,
                                        s =>
                                          s.info
                                            .firstParamTypes
                                            .contains(ctx.definitions.IntType))
                                      .appliedTo(ref(ySymbol.asTerm))
                                  ),
                                  tree.symbol
                                )
                              )
                            )
                          ),
                          CaseDef(
                            Underscore(ctx.definitions.AnyType),
                            EmptyTree,
                            Throw(New(
                              ctx.definitions.IllegalArgumentExceptionType,
                              ctx
                                .definitions
                                .IllegalArgumentExceptionType
                                .member(nme.CONSTRUCTOR)
                                .suchThat(_.info.firstParamTypes match {
                                  case List(pt) =>
                                    pt.isRef(defn.StringClass)
                                  case _ => false
                                })
                                .symbol
                                .asTerm,
                              List(Literal(
                                Constant("call to 'resume' before 'invoke' with coroutine")))
                            ))
                          )
                        )
                      )
                    ),
                    tree.symbol
                  )
                )
              )
            )
          )
        )
      }

  private def transformContinuationWithSuspend(tree: DefDef)(using Context): Tree =
    val newTree =
      tree match
        case HasSuspensionWithDependency(_) => transformSuspendingStateMachine(tree)
        case CallsContinuationResumeWith(resumeArg) =>
          transformSuspendOneContinuationResume(tree, resumeArg)
        case BodyHasSuspensionPoint(_) =>
          cpy.DefDef(tree)() // any suspension that still needs a transformation
        case _ => transformNonSuspending(tree, cpy)

    report.logWith(s"new tree:")(newTree)

  private def transformNonSuspending(tree: DefDef, cpy: TypedTreeCopier)(
      using ctx: Context): DefDef =
    val returnType = OrType(tree.tpt.tpe, ctx.definitions.AnyType, false)
    val completion = generateCompletion(tree.symbol, returnType)
    cpy.DefDef(tree)(
      paramss = params(tree, completion),
      tpt = TypeTree(ctx.definitions.ObjectClass.typeRef))
