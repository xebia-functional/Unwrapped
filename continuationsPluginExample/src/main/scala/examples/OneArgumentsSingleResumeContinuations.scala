package examples

import continuations.Suspend

@main def OneArgumentsSingleResumeContinuations =
  def oneArgumentsSingleResumeContinuations(x: Int)(using s: Suspend): Int =
    s.suspendContinuation[Int] { continuation => continuation.resume(x + 1) }
  println(oneArgumentsSingleResumeContinuations(1))
