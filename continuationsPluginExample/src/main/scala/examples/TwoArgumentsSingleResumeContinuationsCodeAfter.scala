package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsSingleResumeContinuationsCodeAfter =
  def twoArgumentsSingleResumeContinuationsCodeAfter(x: Int, y: Int)(using s: Suspend): Int =
    s.shift(_.resume(println(x)))
    y
  println(SuspendApp(twoArgumentsSingleResumeContinuationsCodeAfter(1, 2)))
