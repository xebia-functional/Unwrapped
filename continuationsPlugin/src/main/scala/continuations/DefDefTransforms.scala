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
   *   The sum of the count and the old count.
   */
  def countContinuationSynthetics(defdef: DefDef, oldCount: Int)(using Context): Int =
    ???

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

  private def transformContinuationWithSuspend(tree: DefDef)(using Context): DefDef =
    val newTree =
      tree match
        case CallsContinuationResumeWith(resumeArg) =>
          transformSuspendOneContinuationResume(tree, resumeArg)
        case BodyHasSuspensionPoint(_) =>
          cpy.DefDef(tree)() // any suspension that still needs a transformation
        case _ => transformNonSuspending(tree, cpy)

    report.logWith(s"new tree:")(newTree)

  def transformSuspendContinuation(tree: DefDef)(using Context): DefDef =
    tree match
      case ReturnsContextFunctionWithSuspendType(_) => transformContinuationWithSuspend(tree)
      case HasSuspendParameter(_) => transformContinuationWithSuspend(tree)
      case _ => report.logWith(s"oldTree:")(tree)

  private def transformNonSuspending(tree: DefDef, cpy: TypedTreeCopier)(
      using ctx: Context): DefDef =
    val returnType = OrType(tree.tpt.tpe, ctx.definitions.AnyType, false)
    val completion = generateCompletion(tree.symbol, returnType)
    cpy.DefDef(tree)(
      paramss = params(tree, completion),
      tpt = TypeTree(ctx.definitions.ObjectClass.typeRef))
