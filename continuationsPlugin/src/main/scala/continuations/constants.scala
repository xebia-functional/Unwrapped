package continuations

private[continuations] val continuationPackageName = "continuations"
private[continuations] val suspendFullName = s"$continuationPackageName.Suspend"
private[continuations] val continuationFullName = s"$continuationPackageName.Continuation"
private[continuations] val suspendContinuationFullName =
  s"$continuationFullName.suspendContinuation"
private[continuations] val resumeFullName = s"$continuationPackageName.Continuation.resume"
private[continuations] val label = "label"
private[continuations] val continuation = "continuation"
