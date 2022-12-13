package continuations

import continuations.DefDefTransforms.*
import dotty.tools.dotc.ast.{tpd, Trees}
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd.TypedTreeCopier
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Annotations.ConcreteAnnotation
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Scopes
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types
import dotty.tools.dotc.core.Types.{MethodType, OrType, Type}
import dotty.tools.dotc.report

import scala.annotation.tailrec
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

object DefDefTransforms extends Trees:

  private def generateCompletion(owner: Symbol, returnType: Type)(using Context): Symbol =
    newSymbol(
      owner,
      Names.termName("completion"),
      Flags.LocalParam,
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

  private def transformContinuationWithSuspend(tree: tpd.DefDef)(using Context): tpd.DefDef =
    val newTree =
      tree match
        // maybe we need to check that CallsContinuationResumeWith/subtreeCallsResume as well?
        case HasSuspensionWithDependency(_) | LastRowNotASuspension(_) =>
          transformSuspendingStateMachine(tree)
        case CallsContinuationResumeWith(resumeArg) =>
          transformSuspendOneContinuationResume(tree, resumeArg)
        case BodyHasSuspensionPoint(_) =>
          cpy.DefDef(tree)() // any suspension that still needs a transformation
        case _ => transformNonSuspending(tree, cpy)

    report.logWith(s"new tree:")(newTree)

  def transformSuspendContinuation(tree: tpd.DefDef)(using Context): tpd.DefDef =
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
        names
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
    val resumesArgs = CallsContinuationResumeWith.unapply(tree).get //
    val defName = tree.symbol.name

    SuspensionPoints
      .unapplySeq(report.logWith("tree has no suspension points:")(tree))
      .fold(tree) { suspensionPoints =>
        val initialCounter =
          ctx.store(ctx.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get)
        val stateMachineContinuationClassName =
          s"${ctx.owner.name.show}$$$defName$$$initialCounter"
        val names = createSyntheticNames(
          ctx.store(
            ctx.property(ContinuationsPhase.continuationsPhaseOldCounterPropertyKey).get),
          suspensionPoints,
          initialCounter - 1,
          Nil
        )
        val continuationNames = names.takeWhile(_.startsWith("continuation"))
        val resultName = names.slice(continuationNames.size, continuationNames.size + 1).head
        val labels = names.drop(continuationNames.size + 1)

        val continuationsStateMachineScope = Scopes.newScope
        val treeOwner = tree.symbol.owner
        val continuationImplClazz = requiredClass("continuations.jvm.internal.ContinuationImpl")

        // class compileFromString-55580126-00e0-477a-8040-f0c76732df77.$package$foo$1
        val continuationsStateMachineSymbol = Symbols.newCompleteClassSymbol(
          treeOwner,
          Names.typeName(stateMachineContinuationClassName),
          Flags.SyntheticArtifact,
          List(continuationImplClazz.typeRef),
          continuationsStateMachineScope,
          Types.NoType,
          treeOwner
        )

        val continuationsStateMachineConstructorMethodCompletionParamName =
          Names.termName("$completion")

        val intType = ctx.definitions.IntType
        val anyType = ctx.definitions.AnyType

        val continuationsStateMachineConstructorMethodSymbol = Symbols.newConstructor(
          continuationsStateMachineSymbol,
          Flags.Synthetic,
          List(continuationsStateMachineConstructorMethodCompletionParamName),
          List(
            requiredClassRef(continuationFullName).appliedTo(OrType(intType, anyType, false)))
        )
        val continuationsStateMachineConstructor =
          tpd.DefDef(continuationsStateMachineConstructorMethodSymbol)

        val continuationsStateMachineResultName = Names.termName("$result")
        val continuationsStateMachineLabelParam = Names.termName("$label")

        val eitherThrowableAnyNullSuspendedType =
          requiredClassRef("scala.util.Either").appliedTo(
            ctx.definitions.ThrowableType,
            Types.OrType(
              Types.OrNull(ctx.definitions.AnyType),
              requiredModuleRef("continuations.Continuation.State.Suspended"),
              false)
          )
        val continuationStateMachineResult = tpd.ValDef(
          continuationsStateMachineScope.enter(
            Symbols.newSymbol(
              continuationsStateMachineSymbol,
              continuationsStateMachineResultName,
              Flags.Synthetic | Flags.Mutable,
              eitherThrowableAnyNullSuspendedType)),
          Underscore(eitherThrowableAnyNullSuspendedType)
        )

        val continuationStateMachineLabel = tpd.ValDef(
          continuationsStateMachineScope.enter(
            Symbols.newSymbol(
              continuationsStateMachineSymbol,
              continuationsStateMachineLabelParam,
              Flags.Synthetic | Flags.Mutable,
              intType)),
          Underscore(intType)
        )

        val anyOrNullType = Types.OrNull(ctx.definitions.AnyType)

        val invokeSuspendSymbol = continuationsStateMachineScope.enter(
          Symbols.newSymbol(
            continuationsStateMachineSymbol,
            Names.termName("invokeSuspend"),
            Flags.Override | Flags.Method,
            anyOrNullType))

        val invokeSuspendResultParam = tpd.ValDef(
          Symbols.newSymbol(
            invokeSuspendSymbol,
            Names.termName("result"),
            Flags.LocalParam,
            eitherThrowableAnyNullSuspendedType),
          tpd.EmptyTree)

        val continuationsStateMachineThis: tpd.This =
          tpd.This(continuationsStateMachineSymbol)

        val continuationsStateMachineLabelSelect =
          continuationsStateMachineThis.select(continuationsStateMachineLabelParam)

        val invokeSuspendMethod = tpd.DefDef(
          invokeSuspendSymbol.asTerm,
          List(List(invokeSuspendResultParam)).map(_.map(_.symbol)),
          anyOrNullType,
          tpd.Block(
            List(
              tpd.Assign(
                continuationsStateMachineThis.select(continuationsStateMachineResultName),
                ref(invokeSuspendResultParam.symbol)),
              tpd.Assign(
                continuationsStateMachineLabelSelect,
                continuationsStateMachineLabelSelect
                  .select(Names.termName("|"))
                  // returns an issue maybe because `|` is synthetic but works?: "value | does not take parameters"
                  .appliedTo(
                    ref(requiredModuleRef("scala.Int").select(Names.termName("MinValue"))))
              )
            ),
            // for now complains because the tree has the old signature but hopefully after the cpy.DefDef it will work
            ref(tree.symbol).appliedTo(
              continuationsStateMachineThis
                .select(Names.termName("asInstanceOf"))
                .appliedToType(
                  requiredClassRef(continuationFullName).appliedTo(ctx.definitions.IntType))
            )
          )
        )

        val $completion = continuationsStateMachineConstructor.paramss.flatten.head.symbol

        val continuationStateMachineClass = ClassDefWithParents(
          continuationsStateMachineSymbol,
          continuationsStateMachineConstructor,
          List(
            ref(continuationImplClazz.primaryConstructor).appliedTo(
              ref($completion),
              ref($completion).select(termName("context"))
            )),
          List(
            continuationStateMachineResult,
            continuationStateMachineLabel,
            invokeSuspendMethod)
        )

        // private[compileFromString-8d546b86-1d3b-4bb7-8781-4399417354a2.$package] class compileFromString-8d546b86-1d3b-4bb7-8781-4399417354a2.$package$foo$1(
        //  $completion: continuations.Continuation[Int | Any]
        // ) extends ContinuationImpl.this($completion, $completion.context) {
        //  var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
        //  var $label: Int = _
        //  override def invokeSuspend(result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type]): Any | Null =
        //    {
        //      this.$result = result
        //      this.$label = this.$label.|(Int.MinValue)
        //      continuations.compileFromString-8d546b86-1d3b-4bb7-8781-4399417354a2.$package.foo(this.asInstanceOf[continuations.Continuation[Int]])
        //    }
        // }
        println(continuationStateMachineClass.show)

        println(s"state machine and new defdef names: $names")
        println(s"state machine and new defdef continuationNames: $continuationNames")
        println(s"state machine and new defdef resultName: $resultName")
        println(s"state machine and new defdef labels: $labels")
//        report.logWith("state machine and new defdef:")(tree)
        tree
      }
