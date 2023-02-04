package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context

/**
 * A matcher for detecting context function returns containing a Suspend type.
 */
private[continuations] object ReturnsContextFunctionWithSuspendType:
  /**
   * Detects context function return types in trees.
   *
   * @param tree
   *   The tree to match upon
   * @param c
   *   A dotty compiler context
   * @return
   *   [[scala.Some]] if the tree returns a context function, [[scala.None]] otherwise.
   */
  def unapply(tree: tpd.ValOrDefDef)(using c: Context): Option[tpd.Tree] =
    Option(tree).filter { t =>
      val tpe = t.tpt.tpe
      IsSuspendContextFunction.unapply(tpe).isDefined || IsSuspendContextFunction
        .unapply(tpe.underlyingIfProxy)
        .isDefined
    }
