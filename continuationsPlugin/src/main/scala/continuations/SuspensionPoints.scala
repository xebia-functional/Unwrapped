package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

case class SuspensionPoints(points: List[Tree])

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
  def unapplySeq(tree: Tree)(using Context): Option[SuspensionPoints] =
    val resultValNonVal = tree
      .shallowFold(List.empty[Tree]) {
        case (suspends, tree) =>
          tree match
            case st @ ValDef(_, _, _) if subtreeCallsSuspend(st.forceIfLazy) =>
              tree :: suspends
            case _ if treeCallsSuspend(tree) =>
              tree :: suspends
            case _ =>
              suspends
      }
      .reverse

    if (resultValNonVal.nonEmpty)
      Some(SuspensionPoints(resultValNonVal))
    else
      Option.empty
