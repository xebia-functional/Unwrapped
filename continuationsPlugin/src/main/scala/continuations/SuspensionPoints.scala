package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.requiredMethod

/**
 * Extracts the trees that suspend from a tree.
 */
object SuspensionPoints extends TreesChecks:
  /**
   * @param tree
   *   The tree to extract the suspension points from.
   * @return
   *   A Some containing a list of [[dotty.tools.dotc.ast.tpd.Tree]]s that suspend from the body
   */
  def unapplySeq(tree: Tree)(using Context): Option[List[Tree]] =
    val result = tree.filterSubTrees {
      case st @ ValDef(_, _, _) => subtreeCallsSuspend(st.forceIfLazy)
      case t: Tree => subtreeCallsSuspend(t)
    }
    if (result.nonEmpty) Some(result) else None
