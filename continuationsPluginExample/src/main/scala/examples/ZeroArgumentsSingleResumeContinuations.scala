package examples

import continuations.Suspend

@main def ZeroArgumentsSingleResumeContinuations =
  def zeroArgumentsSingleResumeContinuations()(using s: Suspend): Int =
    s.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
  println(zeroArgumentsSingleResumeContinuations())
