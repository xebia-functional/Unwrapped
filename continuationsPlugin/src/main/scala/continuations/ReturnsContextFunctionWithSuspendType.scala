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
   *   true if the tree returns a context function, false otherwise.
   */
  def apply(tree: tpd.ValOrDefDef)(using c: Context): Boolean =
    val tpe = tree.tpt.tpe
    IsSuspendContextFunction(tpe) || IsSuspendContextFunction(tpe.underlyingIfProxy)
