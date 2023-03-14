package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.report

/**
 * Matcher for detecting methods that call [[continuations.Suspend#shift]]
 */
private[continuations] object CallsShift extends TreesChecks:

  private def hasNestedContinuation(tree: Tree)(using Context): Boolean =
    tree.existsSubTree {
      case Inlined(call, _, _) =>
        call.denot.matches(shiftMethod.symbol)
      case _ => false
    }

  /**
   * @param tree
   *   the [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @return
   *   true if the tree contains a subtree call to [[continuations.Suspend#shift]], false
   *   otherwise
   */
  def apply(tree: ValOrDefDef)(using Context): Boolean = {
    def isShiftCall(call: Apply): Boolean =
      val isShift = call.denot.matches(shiftMethod.symbol)
      if isShift && hasNestedContinuation(call) then
        report.error(
          "Suspension functions can be called only within coroutine body",
          call.srcPos)
      isShift

    def hasShape(ap: Apply): Boolean = ap match {
      case Apply(_, List(Block(Nil, Block(List(DefDef(_, _, _, suspendBody)), _)))) => true
      case Apply(_, List(Block(List(DefDef(_, _, _, suspendBody)), _))) => true
      case _ => false
    }

    val args = tree.rhs.filterSubTrees {
      case Inlined(call @ Apply(_, _), _, _) => isShiftCall(call) && hasShape(call)
      case _ => false
    }

    args.nonEmpty
  }
