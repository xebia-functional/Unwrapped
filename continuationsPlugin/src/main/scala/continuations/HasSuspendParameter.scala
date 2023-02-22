package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Symbols

/**
 * Matcher for finding [[tpd.DefDef]] trees that have a using [[continuations.Suspend]]
 * parameter.
 */
private[continuations] object HasSuspendParameter:

  /**
   * Matches trees with using [continuations.Suspend] parameters
   *
   * @param tree
   *   The tree to match upon
   * @param c
   *   A dotty compiler context
   * @return
   *   a [[scala.Some]] if the tree has a using [[continuations.Suspend]] parameter,
   *   [[scala.None]] otherwise
   */
  def unapply(tree: tpd.ValOrDefDef)(using c: Context): Option[tpd.Tree] =
    tree match {
      case defDef: tpd.DefDef
          if defDef
            .paramss
            .exists(_.exists { v =>
              v.denot.symbol.is {
                Flags.Given
              } && v.tpe.classSymbol.info.hasClassSymbol(Symbols.requiredClass(suspendFullName))
            }) =>
        Option(tree)
      case _ => None
    }
