package continuations

import continuations.DefDefTransforms.*
import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.TypedTreeCopier
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types
import dotty.tools.dotc.core.Types.OrType
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.report

import scala.annotation.tailrec
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

object DefDefTransforms:

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

    val suspendContinuation: tpd.ValDef =
      tpd.ValDef(
        newSymbol(
          parent,
          termName("suspendContinuation"),
          Flags.Local,
          ctx.definitions.IntType),
        tpd.Literal(Constant(0))
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
        case CallsContinuationResumeWith(resumeArg) =>
          transformSuspendOneContinuationResume(tree, resumeArg)
        case BodyHasSuspensionPoint(_) =>
          cpy.DefDef(tree)() // any suspension that still needs a transformation
        case _ => transformNonSuspending(tree, cpy)

    report.logWith(s"new tree:")(newTree)

  def transformSuspendContinuation(tree: tpd.DefDef)(using Context): tpd.DefDef =
    tree match
      case ReturnsContextFunctionWithSuspendType(_) => transformContinuationWithSuspend(tree)
      case HasSuspendParameter(_) => transformContinuationWithSuspend(tree)
      case _ => report.logWith(s"oldTree:")(tree)

  private def transformNonSuspending(tree: tpd.DefDef, cpy: TypedTreeCopier)(
      using ctx: Context): tpd.DefDef =
    val returnType = Types.OrType(tree.tpt.tpe, ctx.definitions.AnyType, false)
    val completion = generateCompletion(tree.symbol, returnType)
    cpy.DefDef(tree)(
      paramss = params(tree, completion),
      tpt = tpd.TypeTree(ctx.definitions.ObjectClass.typeRef))
