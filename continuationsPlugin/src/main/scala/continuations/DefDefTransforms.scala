package continuations

import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Flags.GivenOrImplicit
import dotty.tools.dotc.core.Flags.Local
import dotty.tools.dotc.core.Flags.LocalParam
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.StdNames.nme.CONSTRUCTOR
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.OrType
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.report
import scala.annotation.tailrec

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
   * result continuation, and a result label for each suspension continuation. So N + 6 +
   * oldCount.
   *
   * @param defdef
   *   The [[dotty.tools.dotc.ast.tpd.DefDef]] to transform
   * @param oldCount
   *   The value of the previous counter.
   * @return
   *   The number of suspension points + 6 + the oldCount.
   */
  def countContinuationSynthetics(defdef: DefDef, oldCount: Int)(using Context): Int =
    defdef match
      case ReturnsContextFunctionWithSuspendType(_) | HasSuspendParameter(_) =>
        defdef match
          case HasSuspensionWithDependency(_) =>
            SuspensionPoints.unapplySeq(defdef).fold(0)(_.size + 6 + oldCount)
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
  def transformSuspendContinuation(tree: DefDef)(using Context): DefDef =
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

    val body = Block(
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

    val body = Block(
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
      case Nil if counter - oldCounter == oldCounter + 5 => createSyntheticNames(oldCounter, Nil, counter - 1, s"${label}result$$$counter" :: names)
      case Nil if counter - oldCounter == oldCounter + 4 => createSyntheticNames(oldCounter, Nil, counter - 1, s"result$$${counter}" :: names)
      case Nil if counter != oldCounter =>
        createSyntheticNames(
          oldCounter,
          suspensionPoints,
          counter - 1,
          s"continuation$$${counter}" :: names)
      case Nil => names

  private def transformSuspendingStateMachine(tree: DefDef)(using ctx: Context) =
    SuspensionPoints.unapplySeq(report.logWith("tree has no suspension points:")(tree)).fold(tree) { suspensionPoints =>
      val initialCounter =
        ctx.store(ctx.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get)
      val stateMachineContinuationClassName =
        s"${ctx.owner.name.show}Continuation$$${initialCounter}"
      val names = createSyntheticNames(
        ctx.store(ctx.property(ContinuationsPhase.continuationsPhaseOldCounterPropertyKey).get),
        suspensionPoints,
        initialCounter - 1,
        Nil
      )
      val continuationNames = names.takeWhile(_.startsWith(continuation))
      val resultName = names.drop(continuationNames.size).take(1).head
      val labels = names.drop(continuationNames.size + 1)
      report.logWith("state machine and new defdef:")(???)
    }

  private def transformContinuationWithSuspend(tree: DefDef)(using Context): DefDef =
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
