package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*

import TreeOps.*

/**
 * Extracts the trees that suspend from a tree.
 */
object SuspensionPoints:

  /**
   * @param tree
   *   The tree to extract the suspension points from.
   * @return
   *   A Some containing a list of [[dotty.tools.dotc.ast.tpd.Tree]]s that suspend from the body
   */
  def unapplySeq(tree: Tree)(using Context): Option[List[Tree]] =
    val treeIsContinuatlionsSuspendContinuation: Context ?=> Tree => Boolean = t =>
      t.denot.matches(requiredMethod(suspendContinuationFullName))

    val result = tree.filterSubTrees {
      case st @ ValDef(_, _, _) => subtreeCallsSuspend(st.forceIfLazy)
      case _ => false
    }
    if (result.nonEmpty) Some(result) else None
