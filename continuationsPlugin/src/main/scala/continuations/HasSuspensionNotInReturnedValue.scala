package continuations

import continuations.BodyHasSuspensionPoint.subtreeCallsSuspend
import continuations.CallsContinuationResumeWith.treeCallsResume
import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*

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
    val callsSuspendAndResume =
      tree.filterSubTrees { treeCallsSuspend }
//        .filter { //test, if it works then keep it commented out, else add it back and remove resumeArgs.isEmpty
//        case Inlined(call, _, _) => subtreesCallsResume(call)
//        case _ => false
//      }

    val lastRowsSuspends =
      (tree.rhs match {
        case Trees.Block(_, tree) => tree
        case tree => tree
      }) match {
        case Trees.Return(expr, _) => treeCallsSuspend(expr)
        case Trees.Inlined(fun, _, _) => treeCallsSuspend(fun)
        case _ => false
      }

    if (callsSuspendAndResume.nonEmpty && !lastRowsSuspends)
      Option(tree)
    else
      Option.empty
