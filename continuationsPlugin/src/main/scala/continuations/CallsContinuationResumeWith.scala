package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.*

/**
 * Matcher for detecting methods that call and [[continuations.Suspend#suspendContinuation]]
 * [[continuations.Continuation.resume
 */
private[continuations] object CallsContinuationResumeWith extends Trees:

  private def hasNestedContinuation(tree: Tree)(using Context): Boolean =
    tree.existsSubTree {
      case Inlined(call, _, _) =>
        call.denot.matches(suspendContinuationMethod.symbol)
      case _ => false
    }

  /**
   * @param tree
   *   the [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @return
   *   [[scala.Some]] if the tree contains a subtree call to
   *   [[continuations.Suspend#suspendContinuation]] and [[continuations.Continuation.resume]],
   *   [[scala.None]] otherwise
   */
  def unapply(tree: DefDef)(using Context): Option[Tree] =
    val args =
      tree
        .rhs
        .filterSubTrees {
          case Inlined(call, _, _) =>
            val isSuspendContinuation = call.denot.matches(suspendContinuationMethod.symbol)
            if isSuspendContinuation && hasNestedContinuation(call) then
              report.error(
                "Suspension functions can be called only within coroutine body",
                call.srcPos)
            isSuspendContinuation
          case _ => false
        }
        .flatMap {
          case Inlined(
                Apply(_, List(Block(Nil, Block(List(DefDef(_, _, _, suspendBody)), _)))),
                _,
                _) =>
            Option(suspendBody)
          case Inlined(Apply(_, List(Block(List(DefDef(_, _, _, suspendBody)), _))), _, _) =>
            Option(suspendBody)
          case _ =>
            None
        }
        .flatMap {
          case Block(Nil, Apply(fun, List(arg))) if treeIsContinuationsResume(fun) =>
            Option(arg.withType(arg.tpe))
          case Apply(fun, List(arg)) if treeIsContinuationsResume(fun) =>
            Option(arg.withType(arg.tpe))
          case _ =>
            None
        }

    Option.when(args.size == 1)(args.head)
