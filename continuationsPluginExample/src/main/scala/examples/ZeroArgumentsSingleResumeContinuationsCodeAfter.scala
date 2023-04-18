package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsSingleResumeContinuationsCodeAfter =
  def zeroArgumentsSingleResumeContinuationsCodeAfter()(using s: Suspend): Int =
    s.shift { completion => completion.resume(println("Hi")) }
    10
  println(SuspendApp(zeroArgumentsSingleResumeContinuationsCodeAfter()))
