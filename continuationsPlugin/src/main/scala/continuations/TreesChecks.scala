package continuations

import dotty.tools.dotc.ast.Trees.{Tree => TTree}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*

trait TreesChecks extends Trees {

  /**
   * @param tree
   *   A [[dotty.tools.dotc.ast.tpd.Tree]] to check if
   *   [[continuations.Suspend#suspendContinuation]] is called
   * @return
   *   True if the calls calls the method [[continuations.Suspend#suspendContinuation]]
   */
  private[continuations] def subtreeCallsSuspend(tree: Tree)(using Context): Boolean =
    val treeIsContinuationsSuspendContinuation: Context ?=> Tree => Boolean = t =>
      t.denot.matches(suspendContinuationMethod.symbol)

    tree.existsSubTree {
      case Inlined(call, _, _) => treeIsContinuationsSuspendContinuation(call)
      case t => treeIsContinuationsSuspendContinuation(t)
    }

  /**
   * @param tree
   *   A [[dotty.tools.dotc.ast.tpd.Tree]] to check if [[continuations.Continuation.resume]] is
   *   called
   * @return
   *   True if the calls calls the method [[continuations.Continuation.resume]]
   */
  private[continuations] def subtreeCallsResume[A](tree: TTree[A])(using Context): Boolean =
    tree.denot.matches(continuationResumeMethod.symbol)
}
