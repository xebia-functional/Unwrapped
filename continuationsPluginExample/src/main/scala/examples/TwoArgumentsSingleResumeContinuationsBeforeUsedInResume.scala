package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsSingleResumeContinuationsBeforeUsedInResume =
  def twoArgumentsSingleResumeContinuationsBeforeUsedInResume(x: Int, y: Int)(
      using s: Suspend): Int =
    println("Hello")
    val z = 1
    s.shift[Int] { continuation => continuation.resume(x + y + z) }
  println(SuspendApp(twoArgumentsSingleResumeContinuationsBeforeUsedInResume(1, 2)))
