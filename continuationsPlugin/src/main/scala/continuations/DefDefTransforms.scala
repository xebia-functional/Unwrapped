package continuations

import continuations.DefDefTransforms.*
import dotty.tools.dotc.ast.{tpd, TreeTypeMap, Trees}
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Symbols, Types}
import dotty.tools.dotc.core.NullOpsDecorator.stripNull
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.{termName, typeName}
import dotty.tools.dotc.core.NameOps.*
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType, Type}
import dotty.tools.dotc.report

import scala.annotation.tailrec
import scala.collection.immutable.List

object DefDefTransforms extends TreesChecks:

  private def generateCompletion(owner: Symbol, returnType: Type)(using Context): Symbol =
    newSymbol(
      owner,
      Names.termName(completionParamName),
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
      (tree.paramss.map {
        _.filterNot {
          case p: Trees.ValDef[Type] =>
            p.typeOpt
              .hasClassSymbol(suspendClazz) && p.symbol.flags.isOneOf(Flags.GivenOrImplicit)
          case t: Trees.TypeDef[Type] =>
            t.typeOpt
              .hasClassSymbol(suspendClazz) && t.symbol.flags.isOneOf(Flags.GivenOrImplicit)
        }.asInstanceOf[tpd.ParamClause]
      }
        ++ List(List(completion).asInstanceOf[tpd.ParamClause])).filterNot(_.isEmpty)

  private def transformSuspendContinuationBody(
      callSuspensionPoint: tpd.Tree,
      safeContinuationRef: tpd.Tree)(using Context): List[tpd.Tree] = {
    val resumeMethod = safeContinuationRef.select(termName("resume"))

    val suspendContinuationBody: List[tpd.Tree] =
      callSuspensionPoint
        .filterSubTrees {
          case Trees.DefDef(_, _, _, _) => true
          case _ => false
        }
        .flatMap {
          case Trees.DefDef(_, _, _, Trees.Block(stats, expr)) =>
            stats.map(s => s.withType(s.typeOpt)) :+ expr.withType(expr.typeOpt)
          case tree =>
            tree.asInstanceOf[tpd.DefDef].rhs :: Nil
        }

    val removedAnonFunc: List[Symbol] = suspendContinuationBody
      .filter(t =>
        t.symbol.exists &&
          t.symbol.owner.isAnonymousFunction &&
          t.symbol
            .owner
            .paramSymss
            .flatten
            .exists(_.info.hasClassSymbol(requiredClass(continuationFullName))))
      .map(_.symbol.owner)
      .distinct

    val suspendContinuationResume = new TreeTypeMap(
      treeMap = {
        case tree @ Trees.Apply(_, List(resumeArg)) if treeCallsResume(tree) =>
          resumeMethod.appliedTo(resumeArg)
        case tree =>
          tree
      },
      oldOwners = removedAnonFunc,
      newOwners = List.fill(removedAnonFunc.size)(safeContinuationRef.symbol.owner),
      substFrom = removedAnonFunc,
      substTo = List.fill(removedAnonFunc.size)(safeContinuationRef.symbol.owner)
    )

    suspendContinuationResume.transformDefs(suspendContinuationBody) match
      case (_, transforms) => transforms
  }

  private def createTransformedMethodSymbol(
      parent: Symbol,
      transformedMethodParams: List[tpd.ParamClause],
      returnType: Type)(using Context) =
    newSymbol(
      parent.owner,
      parent.name.asTermName,
      parent.flags,
      MethodType(
        transformedMethodParams.flatMap {
          _.flatMap {
            case p: tpd.ValDef => List(p.name)
            case _: tpd.TypeDef => List.empty
          }
        },
        transformedMethodParams.flatMap {
          _.flatMap {
            case p: tpd.ValDef => List(p.tpt.tpe)
            case _: tpd.TypeDef => List.empty
          }
        },
        returnType
      ),
      parent.privateWithin,
      parent.coord
    )

  private def getReturnTypeBodyContextFunctionOwner(tree: tpd.DefDef)(
      using Context): (Type, tpd.Tree, Option[Symbol]) =
    if (ReturnsContextFunctionWithSuspendType.unapply(tree).nonEmpty)
      val contextFunctionArgTypes: List[Type] =
        defn
          .asContextFunctionType(tree.tpt.tpe)
          .argTypes
          .filterNot(_.hasClassSymbol(requiredClass(suspendFullName)))

      val returnType =
        if (contextFunctionArgTypes.size > 1)
          Types.AppliedType(contextFunctionArgTypes.head, contextFunctionArgTypes.tail)
        else contextFunctionArgTypes.head

      val (rhs, contextFunctionOwner) = tree.rhs match
        case tpd.Block(List(d @ tpd.DefDef(_, _, _, _)), _) if d.symbol.isAnonymousFunction =>
          (d.rhs, Option(d.symbol))
        case rhs => (rhs, Option.empty)

      (returnType, rhs, contextFunctionOwner)
    else (tree.tpt.tpe, tree.rhs, Option.empty)

  private def transformSuspendOneContinuationResume(
      tree: tpd.DefDef,
      suspensionPoint: tpd.Tree)(using Context): tpd.DefDef =
    val continuationTraitSym: ClassSymbol =
      requiredClass("continuations.Continuation")
    val continuationObjectSym: Symbol =
      continuationTraitSym.companionModule
    val safeContinuationClassSym: ClassSymbol =
      requiredClass("continuations.SafeContinuation")
    val interceptedMethodSym: TermSymbol =
      requiredPackage("continuations.intrinsics").requiredMethod("intercepted")

    val parent: Symbol = tree.symbol

    val (returnType, rhs, contextFunctionOwner) =
      getReturnTypeBodyContextFunctionOwner(tree)

    val continuationTyped: Type =
      continuationTraitSym.typeRef.appliedTo(returnType)

    val suspendedState =
      ref(continuationObjectSym).select(termName("State")).select(termName("Suspended"))

    val finalMethodReturnType: tpd.TypeTree =
      tpd.TypeTree(
        OrType(
          OrType(ctx.definitions.AnyType, ctx.definitions.NullType, soft = false),
          suspendedState.symbol.namedType,
          soft = false)
      )

    val completion = generateCompletion(parent, returnType)

    val transformedMethodParams = params(tree, completion)

    val transformedMethodSymbol =
      createTransformedMethodSymbol(parent, transformedMethodParams, finalMethodReturnType.tpe)

    parent.owner.unforcedDecls.openForMutations.replace(parent, transformedMethodSymbol)

    val transformedMethod =
      tpd.DefDef(sym = transformedMethodSymbol)

    val transformedMethodCompletionParam = ref(
      transformedMethod.termParamss.flatten.find(_.symbol.denot.matches(completion)).get.symbol)

    val continuation1: tpd.ValDef =
      tpd.ValDef(
        sym = newSymbol(
          transformedMethodSymbol,
          termName("continuation1"),
          Flags.Local,
          continuationTyped),
        rhs = transformedMethodCompletionParam)

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
          transformedMethodSymbol,
          termName("safeContinuation"),
          Flags.Local,
          safeContinuationConstructor.tpe),
        safeContinuationConstructor)

    val safeContinuationRef =
      ref(safeContinuation.symbol)

    val callSuspensionPoint: tpd.Tree =
      suspensionPoint match
        case tpd.Inlined(call, _, _) =>
          new TreeTypeMap(
            oldOwners = List(parent),
            newOwners = List(transformedMethod.symbol),
            substFrom = List(parent),
            substTo = List(transformedMethod.symbol)
          ).transform(call)
        case _ => tpd.EmptyTree

    val suspendContinuationBody =
      transformSuspendContinuationBody(callSuspensionPoint, safeContinuationRef)

    val suspendContinuationGetThrow =
      safeContinuationRef.select(termName("getOrThrow")).appliedToNone

    val continuationBlock = tpd.Block(
      List(continuation1, safeContinuation) ++ suspendContinuationBody,
      suspendContinuationGetThrow
    )

    val transformedMethodParamSymbols: List[Symbol] =
      transformedMethodSymbol
        .paramSymss
        .flatten
        .filterNot(_.info.hasClassSymbol(requiredClass(continuationFullName)))

    val oldMethodParamSymbols: List[Symbol] =
      tree.symbol.paramSymss.flatten.filterNot { s =>
        s.info.hasClassSymbol(requiredClass(suspendFullName)) || s.isTypeParam
      }

    val substituteContinuation = new TreeTypeMap(
      treeMap = {
        case tree if treeCallsSuspend(tree) =>
          continuationBlock
        case tree =>
          tree
      },
      substFrom = List(parent) ++ oldMethodParamSymbols ++ contextFunctionOwner.toList,
      substTo = List(transformedMethod.symbol) ++ transformedMethodParamSymbols ++
        List(transformedMethod.symbol),
      oldOwners = List(parent) ++ contextFunctionOwner.toList,
      newOwners = List(transformedMethod.symbol, transformedMethod.symbol)
    )

    cpy.DefDef(transformedMethod)(rhs = substituteContinuation.transform(rhs))

  private def transformContinuationWithSuspend(tree: tpd.DefDef)(using Context): tpd.Tree =
    val newTree =
      tree match
        case HasSuspensionNotInReturnedValue(_) =>
          transformUnusedSuspensionsSuspendingStateMachine(
            tree,
            suspensionInReturnedValue = false)
        case CallsSuspendContinuation(_) =>
          SuspensionPoints.unapplySeq(tree).flatMap(_.nonVal) match
            case Some(suspensionPoint :: Nil) =>
              transformSuspendOneContinuationResume(tree, suspensionPoint)
            case _ =>
              transformUnusedSuspensionsSuspendingStateMachine(
                tree,
                suspensionInReturnedValue = true)
        case BodyHasSuspensionPoint(_) =>
          cpy.DefDef(tree)() // any suspension that still needs a transformation
        case _ => transformNonSuspending(tree, cpy)

    report.logWith(s"new tree:")(newTree)

  def transformSuspendContinuation(tree: tpd.DefDef)(using Context): tpd.Tree =
    tree match
      case ReturnsContextFunctionWithSuspendType(_) =>
        transformContinuationWithSuspend(tree)
      case HasSuspendParameter(_) if IsSuspendContextualMethod.unapply(tree).isEmpty =>
        transformContinuationWithSuspend(tree)
      case _ => report.logWith(s"oldTree:")(tree)

  private def transformNonSuspending(tree: tpd.DefDef, cpy: TypedTreeCopier)(
      using ctx: Context): tpd.DefDef =
    val parent = tree.symbol

    val returnType = Types.OrType(tree.tpt.tpe, ctx.definitions.AnyType, false)
    val completion = generateCompletion(parent, returnType)

    val (_, rhs, contextFunctionOwner) =
      getReturnTypeBodyContextFunctionOwner(tree)

    val transformedMethodParams = params(tree, completion)

    val transformedMethodSymbol =
      createTransformedMethodSymbol(parent, transformedMethodParams, defn.AnyType)

    parent.owner.unforcedDecls.openForMutations.replace(parent, transformedMethodSymbol)

    val transformedMethod =
      tpd.DefDef(sym = transformedMethodSymbol)

    val transformedMethodParamSymbols: List[Symbol] =
      transformedMethodSymbol
        .paramSymss
        .flatten
        .filterNot(_.info.hasClassSymbol(requiredClass(continuationFullName)))

    val oldMethodParamSymbols: List[Symbol] =
      parent.paramSymss.flatten.filterNot { s =>
        s.info.hasClassSymbol(requiredClass(suspendFullName)) || s.isTypeParam
      }

    val substituteContinuation = new TreeTypeMap(
      substFrom = List(parent) ++ oldMethodParamSymbols ++ contextFunctionOwner.toList,
      substTo = List(transformedMethod.symbol) ++ transformedMethodParamSymbols ++
        List(transformedMethod.symbol),
      oldOwners = List(parent) ++ contextFunctionOwner.toList,
      newOwners = List(transformedMethod.symbol, transformedMethod.symbol)
    )

    cpy.DefDef(transformedMethod)(rhs = substituteContinuation.transform(rhs))

  private def transformUnusedSuspensionsSuspendingStateMachine(
      tree: tpd.DefDef,
      suspensionInReturnedValue: Boolean)(using ctx: Context) =
    SuspensionPoints
      .unapplySeq(report.logWith("tree has no suspension points:")(tree))
      .flatMap(_.nonVal) match
      case None | Some(Nil) => tree
      case Some(suspensionPoints) =>
        val suspensionPointsSize = suspensionPoints.size

        val continuationClassRef = requiredClassRef(continuationFullName)
        val continuationModule = requiredModule(continuationFullName)
        val safeContinuationClass = requiredClass("continuations.SafeContinuation")
        val interceptedMethod =
          requiredPackage("continuations.intrinsics").requiredMethod("intercepted")
        val continuationImplClass = requiredClass("continuations.jvm.internal.ContinuationImpl")

        val parent = tree.symbol
        val treeOwner = parent.owner
        val defName = parent.name

        val (returnType, rhs, contextFunctionOwner) =
          getReturnTypeBodyContextFunctionOwner(tree)

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

        val stateMachineContinuationClassName =
          s"${treeOwner.name.show}$$$defName$$1"

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
          createTransformedMethodSymbol(parent, transformedMethodParams, newReturnType.tpe)

        treeOwner.unforcedDecls.openForMutations.replace(parent, transformedMethodSymbol)

        val transformedMethod: tpd.DefDef =
          tpd.DefDef(sym = transformedMethodSymbol)

        val transformedMethodCompletionParam = ref(
          transformedMethod
            .termParamss
            .flatten
            .find(_.symbol.denot.matches(completion))
            .get
            .symbol)

        val transformedMethodOriginalParams =
          transformedMethod.termParamss.flatten.init

        val invokeSuspendMethod = tpd.DefDef(
          invokeSuspendSymbol,
          paramss =>
            tpd.Block(
              List(
                tpd.Assign(
                  continuationsStateMachineThis.select(continuationsStateMachineResultName),
                  paramss.head.head),
                tpd.Assign(
                  continuationsStateMachineLabelSelect,
                  continuationsStateMachineLabelSelect.select(integerOR).appliedTo(integerMin)
                )
              ),
              ref(transformedMethod.symbol).appliedToTermArgs(
                List.fill(transformedMethodOriginalParams.size)(nullLiteral) :+
                  continuationsStateMachineThis
                    .select(nme.asInstanceOf_)
                    .appliedToType(continuationClassRef.appliedTo(returnType))
              )
            )
        )

        val $completion = continuationsStateMachineConstructor
          .termParamss
          .flatten
          .find(
            _.name.matchesTargetName(
              continuationsStateMachineConstructorMethodCompletionParamName))
          .get
          .symbol

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

        val rowsBeforeSuspensionPoints: Map[Int, List[tpd.Tree]] =
          rhs
            .toList
            .flatMap {
              case Trees.Block(trees, tree) => trees :+ tree
              case tree => List(tree)
            }
            .foldLeft(0 -> Map.empty[Int, List[tpd.Tree]]) {
              case ((suspensionPointsCounter, rowsBefore), row) =>
                if (treeCallsSuspend(row))
                  (suspensionPointsCounter + 1, rowsBefore)
                else if (suspensionPointsCounter < suspensionPointsSize)
                  (
                    suspensionPointsCounter,
                    rowsBefore.updatedWith(suspensionPointsCounter) {
                      case Some(ll) => Option(ll :+ row)
                      case None => Option(row :: Nil)
                    })
                else
                  (suspensionPointsCounter, rowsBefore)
            }
            ._2

        def transformSuspendTree(newParent: Symbol) = {
          val $continuation = tpd.ValDef(
            newSymbol(
              newParent,
              termName("$continuation"),
              Local | Mutable | Synthetic,
              OrType(continuationClassRef.appliedTo(anyType), defn.NullType, soft = false)),
            nullLiteral
          )

          val continuationAsStateMachineClass =
            ref($continuation.symbol)
              .select(nme.asInstanceOf_)
              .appliedToType(continuationStateMachineClass.tpe)

          val case11Param =
            newSymbol(newParent, nme.x_0, Flags.Case | Flags.CaseAccessor, defn.AnyType)
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

          val completionMatch =
            tpd.Match(transformedMethodCompletionParam, List(case11, case12))

          val $result =
            tpd.ValDef(
              newSymbol(
                newParent,
                termName("$result"),
                Local,
                eitherThrowableAnyNullSuspendedType),
              continuationAsStateMachineClass.select(continuationsStateMachineResultName)
            )

          val undecidedState =
            ref(continuationModule).select(termName("State")).select(termName("Undecided"))

          def throwOnFailure =
            tpd.If(
              ref($result.symbol).select(nme.NotEquals).appliedTo(nullLiteral),
              ref($result.symbol)
                .select(termName("fold"))
                .appliedToType(defn.UnitType)
                .appliedTo(
                  tpd.Lambda(
                    MethodType.apply(List(defn.ThrowableType))(_ => defn.NothingType),
                    trees => tpd.Throw(trees.head)),
                  tpd.Lambda(
                    MethodType(List(anyNullSuspendedType))(_ => defn.UnitType),
                    _ => unitLiteral)
                ),
              unitLiteral
            )

          val labels: List[Symbol] =
            suspensionPoints.indices.toList.map { i =>
              newSymbol(newParent, termName(s"label$i"), Flags.Label, defn.UnitType)
            }

          val cases =
            suspensionPoints.indices.toList.map { i =>
              val label =
                if (i > 0)
                  tpd.Labeled(labels(i).asTerm, tpd.EmptyTree)
                else
                  tpd.EmptyTree

              val returnToLabel =
                if (suspensionPointsSize > 1 && i < suspensionPointsSize - 1)
                  tpd.Return(unitLiteral, labels(i + 1))
                else
                  tpd.EmptyTree

              val callSuspensionPoint: tpd.Tree = suspensionPoints(i) match
                case tpd.Inlined(call, _, _) =>
                  new TreeTypeMap(
                    oldOwners = List(parent),
                    newOwners = List(newParent),
                    substFrom = List(parent),
                    substTo = List(newParent)
                  ).transform(call)
                case _ => tpd.EmptyTree

              val suspendContinuationType = callSuspensionPoint.tpe
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
                    newParent,
                    termName("safeContinuation"),
                    Flags.Local,
                    safeContinuationConstructor.tpe),
                  safeContinuationConstructor)

              val safeContinuationRef =
                ref(safeContinuation.symbol)

              val suspendContinuationBody =
                transformSuspendContinuationBody(callSuspensionPoint, safeContinuationRef)

              val suspendContinuationGetThrow =
                tpd.ValDef(
                  newSymbol(newParent, termName("orThrow"), Flags.Local, anyNullSuspendedType),
                  safeContinuationRef.select(termName("getOrThrow")).appliedToNone
                )

              val ifOrThrowReturn =
                tpd.If(
                  ref(suspendContinuationGetThrow.symbol)
                    .select(nme.Equals)
                    .appliedTo(suspendedState),
                  tpd.Return(suspendedState, newParent),
                  tpd.EmptyTree
                )

              val resultValue =
                if (i == suspensionPointsSize - 1 && suspensionInReturnedValue)
                  ref(suspendContinuationGetThrow.symbol)
                else unitLiteral

              tpd.CaseDef(
                tpd.Literal(Constant(i)),
                tpd.EmptyTree,
                tpd.Block(
                  List(throwOnFailure, label) ++
                    rowsBeforeSuspensionPoints.get(i).toList.flatten ++
                    List(
                      tpd.Assign(
                        continuationAsStateMachineClass.select(
                          continuationsStateMachineLabelParam),
                        tpd.Literal(Constant(i + 1))),
                      safeContinuation) ++
                    suspendContinuationBody ++
                    List(suspendContinuationGetThrow, ifOrThrowReturn, returnToLabel),
                  resultValue
                )
              )
            }

          val case2BeforeDefault = tpd.CaseDef(
            tpd.Literal(Constant(suspensionPointsSize)),
            tpd.EmptyTree,
            throwOnFailure
          )

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

          val case2Default = tpd.CaseDef(
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

          val labelMatch = tpd
            .Match(
              continuationAsStateMachineClass.select(continuationsStateMachineLabelParam),
              cases ++ List(case2BeforeDefault, case2Default)
            )
            .withType(anyNullSuspendedType)

          tpd.Block(List($continuation, completionMatch, $result), labelMatch)
        }

        val transformedMethodParamSymbols: List[Symbol] =
          transformedMethodSymbol
            .paramSymss
            .flatten
            .filterNot(_.info.hasClassSymbol(requiredClass(continuationFullName)))

        val oldMethodParamSymbols: List[Symbol] =
          tree.symbol.paramSymss.flatten.filterNot { s =>
            s.info.hasClassSymbol(requiredClass(suspendFullName)) || s.isTypeParam
          }

        /**
         * If there are more that one Suspension points we create the whole state machine for
         * all the points and replace the last occurrence of `suspendContinuation`. The other
         * occurrences are not needed and are being removed.
         *
         * We also don't need to keep the rest of the lines before the last
         * `suspendContinuation` as they are embedded in the state machine.
         */
        var c = 0
        var rowsToRemove = rowsBeforeSuspensionPoints.values.toList.flatten.map(_.symbol.coord)
        val substituteContinuation = new TreeTypeMap(
          treeMap = {
            case tree @ Trees.Inlined(_, _, _)
                if treeCallsSuspend(tree) && c < suspensionPointsSize - 1 =>
              c += 1
              tpd.EmptyTree
            case tree @ Trees.Inlined(_, _, _)
                if treeCallsSuspend(tree) && c == suspensionPointsSize - 1 =>
              transformSuspendTree(transformedMethod.symbol)
            case tree if c < suspensionPointsSize && rowsToRemove.contains(tree.symbol.coord) =>
              rowsToRemove = rowsToRemove.zipWithIndex.collect {
                case (coord, index) if index != rowsToRemove.indexOf(tree.symbol.coord) =>
                  coord
              }
              tpd.EmptyTree
            case tree => tree
          },
          substFrom = List(parent) ++ oldMethodParamSymbols ++ contextFunctionOwner.toList,
          substTo = List(transformedMethod.symbol) ++ transformedMethodParamSymbols ++
            List(transformedMethod.symbol),
          oldOwners = List(parent) ++ contextFunctionOwner.toList,
          newOwners = List(transformedMethod.symbol, transformedMethod.symbol)
        )

        val transformedTree =
          tpd.Thicket(
            continuationStateMachineClass ::
              cpy.DefDef(transformedMethod)(rhs = substituteContinuation.transform(rhs)) :: Nil)

        report.logWith("state machine and new defdef:")(transformedTree)
