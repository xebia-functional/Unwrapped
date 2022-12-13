package continuations

import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*

/**
 * Matcher for detecting methods that calls/returns
 * [[continuations.Suspend#suspendContinuation]] in their last row
 */
private[continuations] object LastRowNotASuspension extends TreesChecks:

  /**
   * @param tree
   *   the [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @return
   *   [[scala.Some]] if the tree calls/returns [[continuations.Suspend#suspendContinuation]] in
   *   the last row, [[scala.None]] otherwise
   */
  def unapply(tree: DefDef)(using Context): Option[Tree] =
    val lastRowsSuspends =
      (tree.rhs match {
        case Trees.Block(_, tree) => tree
        case tree => tree
      }) match {
        case Trees.Return(Trees.Inlined(fun, _, _), _) =>
          fun.denot.matches(suspendContinuationMethod.symbol)
        case Trees.Inlined(fun, _, _) =>
          fun.denot.matches(suspendContinuationMethod.symbol)
        case _ => false
      }

    if (lastRowsSuspends)
      Option.empty
    else
      Option(tree)
