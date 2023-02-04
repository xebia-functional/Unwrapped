package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.requiredClass
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
   *   a [[scala.Some]] if the tree has a given [[continuations.Suspend]] parameter,
   *   [[scala.None]] otherwise
   */
  def unapply(tree: tpd.Tree)(using c: Context): Option[tpd.Tree] =
    tree match
      case tpd.Apply(_, args) if args.map(_.symbol).exists { s =>
            s.is(Flags.Given) &&
            s.info.classSymbol.info.hasClassSymbol(requiredClass(suspendFullName))
          } =>
        Option(tree)
      case _ => None
