package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsSingleResumeContinuations =
  def oneArgumentsSingleResumeContinuations(x: Int)(using s: Suspend): Int =
    s.shift[Int] { continuation => continuation.resume(x + 1) }
  println(SuspendApp(oneArgumentsSingleResumeContinuations(1)))
