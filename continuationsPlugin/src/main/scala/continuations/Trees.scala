package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Symbols.requiredClass

trait Trees {
  private[continuations] def suspendContinuationMethod(using Context): Select =
    ref(requiredClass(suspendFullName)).select(termName(suspendContinuationName))
}
