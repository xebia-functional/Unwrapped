package examples

import continuations.Suspend

@main def TwoArgumentsSingleResumeContinuations =
  def twoArgumentsSingleResumeContinuations(x: Int, y: Int)(using s: Suspend): Int =
    s.suspendContinuation[Int] { continuation => continuation.resume(Right(x + y + 1)) }
  println(twoArgumentsSingleResumeContinuations(1, 2))
