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
   *   A [[scala.Some]] if the method has a using [[continuations.Suspend]] parameter and has a
   *   suspended continuation in its method body, [[scala.None]] otherwise
   */
  def unapply(defdef: DefDef)(using Context): Option[DefDef] =
    if (HasSuspendParameter.unapply(defdef).isDefined && subtreeCallsSuspend(defdef.rhs))
      Option(defdef)
    else
      None
