package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Symbols.requiredClass

private[continuations] val continuationPackageName = "continuations"
private[continuations] val shiftName = "shift"
private[continuations] val resumeName = "resume"
private[continuations] val suspendFullName = s"$continuationPackageName.Suspend"
private[continuations] val continuationFullName = s"$continuationPackageName.Continuation"
private[continuations] val completionParamName = "completion"
private[continuations] val continuationClassName = "Continuation"
private[continuations] val resumeMethodName = "resume"
private[continuations] val raiseMethodName = "raise"
private[continuations] val starterClassName = s"$continuationPackageName.jvm.internal.Starter"
private[continuations] val continuationImplFullName = "continuations.jvm.internal.ContinuationImpl"
private [continuations] val $resultName = "$result"
private [continuations] val $labelName = "$label"
private [continuations] val invokeSuspendName = "invokeSuspend"
private [continuations] val minValueName = "MinValue"
private [continuations] val baseContinuationImplFullName = "continuations.jvm.internal.BaseContinuationImpl"
private [continuations] val createName = "create"
private [continuations] val contextName = "context"
private [continuations] val stateName = "State"
private [continuations] val suspendedName = "Suspended"
