package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Symbols.requiredClass

private[continuations] val continuationPackageName = "continuations"
private[continuations] val suspendContinuationName = "suspendContinuation"
private[continuations] val resumeName = "resume"
private[continuations] val suspendFullName = s"$continuationPackageName.Suspend"
private[continuations] val continuationFullName = s"$continuationPackageName.Continuation"
private[continuations] val completionParamName = "completion"
