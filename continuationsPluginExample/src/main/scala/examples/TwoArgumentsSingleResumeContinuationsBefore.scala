package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsSingleResumeContinuationsBefore =
  def twoArgumentsSingleResumeContinuationsBefore(x: Int, y: Int)(using s: Suspend): Int =
    println("Hello")
    val z = x + y
    s.shift(_.resume(1))
  println(SuspendApp(twoArgumentsSingleResumeContinuationsBefore(1, 2)))
