package continuations

import continuations.Types.flattenTypes
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types.Type

/**
 * A matcher for detecting suspend context function parameters in a type.
 */
object IsSuspendContextFunction:
  /**
   * @param typ
   *   The [[dotty.tools.dotc.core.Types.Type]] to match upon
   * @param c
   *   A dotty compiler context
   * @return
   *   [[scala.Some]] if the type is a contextFunction containing a [[continuations.Suspend]]
   *   context function parameter, [[scala.None]] otherwise
   */
  def apply(typ: Type)(using c: Context): Boolean =
    val isSuspendClass = (t: Type) => t.hasClassSymbol(Symbols.requiredClass(suspendFullName))

    def isSCF(typ: Type): Boolean =
      val argType = c.definitions.asContextFunctionType(typ).argTypes
      c.definitions.isContextFunctionType(typ) &&
      argType.zipWithIndex.exists { (tpe, index) =>
        isSuspendClass(tpe) && index != argType.length - 1
      }

    flattenTypes(typ).exists(isSCF)
