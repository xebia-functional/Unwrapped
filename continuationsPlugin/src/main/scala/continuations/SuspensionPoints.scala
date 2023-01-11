package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

case class SuspensionPoints(withVal: Option[List[Tree]], nonVal: Option[List[Tree]]) {
  def totalSize: Int =
    withValSize + nonValSize

  def withValSize: Int =
    withVal.fold(0)(_.size)

  def nonValSize: Int =
    nonVal.fold(0)(_.size)
}

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
  def unapplySeq(tree: Tree)(using Context): Option[SuspensionPoints] =
    val resultVal = tree.filterSubTrees {
      case st @ ValDef(_, _, _) => subtreeCallsSuspend(st.forceIfLazy)
      case _ => false
    }

    val resultNonVal = tree
      .filterSubTrees {
        case ValDef(_, _, _) => false
        case t => treeCallsSuspend(t)
      }
      .diff(resultVal.flatMap(_.filterSubTrees(_ => true)))

    (resultVal, resultNonVal) match
      case (Nil, Nil) => None
      case (Nil, _) => Some(SuspensionPoints(None, Some(resultNonVal)))
      case (_, Nil) => Some(SuspensionPoints(Some(resultVal), None))
      case (_, _) => Some(SuspensionPoints(Some(resultVal), Some(resultNonVal)))
