package continuations

import continuations.BodyHasSuspensionPoint.subtreeCallsSuspend
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

/**
 * Detects trees with continuation-dependent calculations
 */
object HasSuspensionWithDependency:
  /**
   * @param tree
   *   A [[dotty.tools.dotc.ast.tpd.Tree]] to check for continuation dependent calculations
   * @return
   *   Some(tree) if the tree has a continuation and a calculation dependent upon that
   *   calculation. None
   */
  def unapply(tree: DefDef)(using Context): Option[Tree] =
    if (CallsSuspendContinuation.unapply(tree).nonEmpty &&
      tree
        .filterSubTrees {
          case Inlined(call, _, _) => subtreeCallsSuspend(call)
          case t => subtreeCallsSuspend(t)
        }
        .exists { t =>
          tree.existsSubTree {
            case Inlined(call, _, _) =>
              call.denot.containsSym(t.symbol) && !call.sameTree(t)
            case tt =>
              tt.denot.containsSym(t.symbol) && !tt.sameTree(t)
          }
        }) {
      Option(tree)
    } else None
