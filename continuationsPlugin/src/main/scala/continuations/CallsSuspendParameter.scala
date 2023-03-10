package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.{requiredClass, Symbol}
import dotty.tools.dotc.core.Flags

/**
 * Matcher for finding [[tpd.Apply]] trees that have a given [[continuations.Suspend]]
 * parameter.
 */
private[continuations] object CallsSuspendParameter:

  /**
   * Matches trees with `given [continuations.Suspend]` parameters
   *
   * @param tree
   *   The tree to match upon
   * @param c
   *   A dotty compiler context
   * @return
   *   true if the tree has a given [[continuations.Suspend]] parameter, false otherwise
   */
  def apply(tree: tpd.Tree)(using c: Context): Boolean =
    def isGivenSuspend(s: Symbol): Boolean =
      s.is(Flags.Given) && s
        .info
        .classSymbol
        .info
        .hasClassSymbol(requiredClass(suspendFullName))
    tree match
      case tpd.Apply(_, args) => args.exists(a => isGivenSuspend(a.symbol))
      case _ => false
