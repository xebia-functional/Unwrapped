package continuations

import dotty.tools.dotc.core.Types.*
import dotty.tools.dotc.core.Contexts.Context

extension(tpe: Type)
  private [continuations] infix def |(or: Type)(using Context): Type = OrType(tpe, or, false)
  private [continuations] def ?(using Context): Type = OrNull(tpe)
