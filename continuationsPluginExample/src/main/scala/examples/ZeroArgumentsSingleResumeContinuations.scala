package examples

import continuations.Suspend

@main def ZeroArgumentsSingleResumeContinuations =
  def zeroArgumentsSingleResumeContinuations()(using s: Suspend): Int =
    s.shift[Int] { continuation => continuation.resume(Right(1)) }
  println(zeroArgumentsSingleResumeContinuations())
