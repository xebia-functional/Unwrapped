package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*

private[continuations] def subtreeCallsSuspend(tree: Tree)(using Context): Boolean =
  val treeIsContinuationsSuspendContinuation: Context ?=> Tree => Boolean = t =>
    t.denot.matches(suspendContinuationMethod.symbol)

  tree.existsSubTree {
    case Inlined(call, _, _) => treeIsContinuationsSuspendContinuation(call)
    case t => treeIsContinuationsSuspendContinuation(t)
  }
