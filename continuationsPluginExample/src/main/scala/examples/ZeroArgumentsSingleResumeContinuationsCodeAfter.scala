package examples

import continuations.Suspend

@main def ZeroArgumentsSingleResumeContinuationsCodeAfter =
  def zeroArgumentsSingleResumeContinuationsCodeAfter()(using s: Suspend): Int =
    s.shift { completion => completion.resume(Right(println("Hi"))) }
    10
  println(zeroArgumentsSingleResumeContinuationsCodeAfter())
