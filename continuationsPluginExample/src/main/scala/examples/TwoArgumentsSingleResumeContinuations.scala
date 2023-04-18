package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsSingleResumeContinuations =
  def twoArgumentsSingleResumeContinuations(x: Int, y: Int)(using s: Suspend): Int =
    s.shift[Int] { continuation => continuation.resume(x + y + 1) }
  println(SuspendApp(twoArgumentsSingleResumeContinuations(1, 2)))
