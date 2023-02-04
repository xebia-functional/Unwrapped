package continuations

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types.Type

/**
 * A matcher for detecting suspend context function parameters in a type.
 */
object IsSuspendContextFunction:
  /**
   * @param t
   *   The [[dotty.tools.dotc.core.Types.Type]] to match upon
   * @param c
   *   A dotty compiler context
   * @return
   *   [[scala.Some]] if the type is a contextFunction containing a [[continuations.Suspend]]
   *   context function parameter, [[scala.None]] otherwise
   */
  def unapply(t: Type)(using c: Context): Option[Type] =
    val isContextFunction = c.definitions.isContextFunctionType(t)
    val argTypes = c.definitions.asContextFunctionType(t).argTypes
    val isSuspendClass = (t: Type) => t.hasClassSymbol(Symbols.requiredClass(suspendFullName))
    if (isContextFunction && argTypes.zipWithIndex.exists { (tpe, index) =>
        isSuspendClass(tpe) && index != argTypes.length - 1
      }) Option(t)
    else None
