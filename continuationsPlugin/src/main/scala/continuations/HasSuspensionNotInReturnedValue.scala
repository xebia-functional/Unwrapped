package continuations

import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

/**
 * Matcher for detecting methods that calls/returns
 * [[continuations.Suspend#suspendContinuation]] but not in their last row
 */
private[continuations] object HasSuspensionNotInReturnedValue extends TreesChecks:

  /**
   * @param tree
   *   the [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @return
   *   [[scala.Some]] with the tree if it calls [[continuations.Suspend#suspendContinuation]]
   *   and [[continuations.Continuation.resume]] but not in the last row, [[scala.None]]
   *   otherwise
   */
  def unapply(tree: DefDef)(using Context): Option[Tree] =
    def lastRowsSuspends =
      (tree.rhs match {
        case Trees.Block(_, tree) => tree
        case tree => tree
      }) match {
        case Trees.Return(expr, _) => treeCallsSuspend(expr)
        case t => treeCallsSuspend(t)
      }

    if (CallsContinuationResumeWith.unapply(tree).nonEmpty && !lastRowsSuspends)
      Option(tree)
    else
      Option.empty
