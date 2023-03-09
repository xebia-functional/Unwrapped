package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Symbols.{requiredClass, requiredClassRef}

trait Trees {
  private[continuations] def shiftMethod(using Context): Select =
    ref(requiredClass(suspendFullName)).select(termName(shiftName))

  private[continuations] def continuationResumeMethod(using Context) =
    requiredClassRef(continuationFullName).select(termName(resumeName))
}
