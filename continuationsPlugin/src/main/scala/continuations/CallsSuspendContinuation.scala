package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.report

/**
 * Matcher for detecting methods that call [[continuations.Suspend#suspendContinuation]]
 */
private[continuations] object CallsSuspendContinuation extends TreesChecks:

  private def hasNestedContinuation(tree: Tree)(using Context): Boolean =
    tree.existsSubTree {
      case Inlined(call, _, _) =>
        call.denot.matches(suspendContinuationMethod.symbol)
      case _ => false
    }

  /**
   * @param tree
   *   the [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @return
   *   [[scala.Some]] if the tree contains a subtree call to
   *   [[continuations.Suspend#suspendContinuation]], [[scala.None]] otherwise
   */
  def apply(tree: ValOrDefDef)(using Context): Boolean =
    val args =
      tree
        .rhs
        .filterSubTrees {
          case Inlined(call, _, _) =>
            val isSuspendContinuation = call.denot.matches(suspendContinuationMethod.symbol)
            if isSuspendContinuation && hasNestedContinuation(call) then
              report.error(
                "Suspension functions can be called only within coroutine body",
                call.srcPos)
            isSuspendContinuation
          case _ => false
        }
        .flatMap {
          case Inlined(
                Apply(_, List(Block(Nil, Block(List(DefDef(_, _, _, suspendBody)), _)))),
                _,
                _) =>
            Option(suspendBody)
          case Inlined(Apply(_, List(Block(List(DefDef(_, _, _, suspendBody)), _))), _, _) =>
            Option(suspendBody)
          case _ =>
            None
        }

    args.nonEmpty
