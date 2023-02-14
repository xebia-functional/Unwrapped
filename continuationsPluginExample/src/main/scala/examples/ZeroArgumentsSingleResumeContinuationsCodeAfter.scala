package examples

import continuations.Suspend

@main def ZeroArgumentsSingleResumeContinuationsCodeAfter =
  def zeroArgumentsSingleResumeContinuationsCodeAfter()(using s: Suspend): Int =
    s.suspendContinuation { completion => completion.resume(Right(println("Hi"))) }
    10
  println(zeroArgumentsSingleResumeContinuationsCodeAfter())
