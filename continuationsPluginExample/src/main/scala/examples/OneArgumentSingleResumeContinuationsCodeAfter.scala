package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentSingleResumeContinuationsCodeAfter =
  def oneArgumentSingleResumeContinuationsCodeAfter(str: String)(using s: Suspend): Int =
    s.shift(_.resume(println(str)))
    10
  println(SuspendApp(oneArgumentSingleResumeContinuationsCodeAfter("Hello")))
