package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

/**
 * Extracts the trees that suspend from a tree, either assigned to a val or not.
 */
object SuspensionPoints extends TreesChecks:
  /**
   * @param tree
   *   The tree to extract the suspension points from.
   * @return
   *   A Some containing a list of [[dotty.tools.dotc.ast.tpd.Tree]]s that suspend from the body
   */
  def unapplySeq(tree: Tree)(using Context): Option[List[Tree]] =
    val resultValNonVal = tree
      .shallowFold(List.empty[Tree]) {
        case (suspends, tree) =>
          tree match
            case vd: ValDef if subtreeCallsSuspend(vd.forceIfLazy) =>
              tree :: suspends
            case _ if treeCallsSuspend(tree) =>
              tree :: suspends
            case _ =>
              suspends
      }
      .reverse

    if (resultValNonVal.nonEmpty)
      Some(resultValNonVal)
    else
      Option.empty
