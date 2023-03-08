package continuations

import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

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
  def apply(tree: ValOrDefDef)(using Context): Boolean =
    val rhs =
      if (ReturnsContextFunctionWithSuspendType(tree))
        tree.rhs match
          case Block(List(d @ DefDef(_, _, _, _)), _) if d.symbol.isAnonymousFunction => d.rhs
          case rhs => rhs
      else
        tree.rhs

    val returnsSuspend =
      (rhs.toList.filter(treeCallsSuspend) ++
        rhs
          .filterSubTrees(t => subtreeCallsSuspend(t) && !treeCallsSuspend(t))
          .takeRight(rhs.filterSubTrees(treeCallsSuspend).size)).exists {
        case Trees.Block(_, expr) =>
          treeCallsSuspend(expr)
        case Trees.Return(expr, _) =>
          treeCallsSuspend(expr)
        case Trees.If(_, thenp, elsep) =>
          treeCallsSuspend(thenp) || treeCallsSuspend(elsep)
        case Trees.Closure(_, meth, _) =>
          treeCallsSuspend(meth)
        case Trees.CaseDef(_, _, body) =>
          treeCallsSuspend(body)
        case Trees.WhileDo(_, body) =>
          treeCallsSuspend(body)
        case Trees.TermLambdaTypeTree(_, body) =>
          treeCallsSuspend(body)
        case tree =>
          treeCallsSuspend(tree)
      }

    CallsSuspendContinuation(tree) && !returnsSuspend
