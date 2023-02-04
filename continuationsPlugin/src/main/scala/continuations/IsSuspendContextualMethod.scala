package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types.{MethodTpe, Type}

/**
 * A matcher for detecting suspend contextual methods
 */
object IsSuspendContextualMethod:
  /**
   * @param tree
   *   The [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @param c
   *   A dotty compiler context
   * @return
   *   [[scala.Some]] if the tree is a contextualMethod containing a [[continuations.Suspend]]
   *   context function parameter, [[scala.None]] otherwise
   */
  def unapply(tree: tpd.Tree)(using c: Context): Option[tpd.Tree] =
    val isSuspendClass = (t: Type) => t.hasClassSymbol(Symbols.requiredClass(suspendFullName))
    val t = tree.symbol.info

    val isContextualMethod = t.isContextualMethod && (t match
      case MethodTpe(_, params, _) => params.exists(isSuspendClass)
      case _ => false
    )

    if (isContextualMethod) Option(tree)
    else None
