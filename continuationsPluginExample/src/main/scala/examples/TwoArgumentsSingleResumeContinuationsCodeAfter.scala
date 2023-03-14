package examples

import continuations.Suspend

@main def TwoArgumentsSingleResumeContinuationsCodeAfter =
  def twoArgumentsSingleResumeContinuationsCodeAfter(x: Int, y: Int)(using s: Suspend): Int =
    s.suspendContinuation(_.resume(println(x)))
    y
  println(twoArgumentsSingleResumeContinuationsCodeAfter(1, 2))
