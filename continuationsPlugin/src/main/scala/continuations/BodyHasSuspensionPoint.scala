package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*

/**
 * A matcher for identifying methods with suspending continuations in their bodies.
 */
private[continuations] object BodyHasSuspensionPoint extends TreesChecks:

  /**
   * Matches methods with a using [[continuations.Suspend]] parameter that suspend a
   * continuation in their method body.
   *
   * @param defdef
   *   The method to match upon
   * @return
   *   A true if the method has a using [[continuations.Suspend]] parameter and has a suspended
   *   continuation in its method body, false otherwise
   */
  def apply(defdef: DefDef)(using Context): Boolean =
    HasSuspendParameter(defdef) && subtreeCallsSuspend(defdef.rhs)
