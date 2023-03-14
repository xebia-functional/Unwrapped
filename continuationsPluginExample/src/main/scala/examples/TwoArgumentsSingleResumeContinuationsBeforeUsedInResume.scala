package examples

import continuations.Suspend

@main def TwoArgumentsSingleResumeContinuationsBeforeUsedInResume =
  def twoArgumentsSingleResumeContinuationsBeforeUsedInResume(x: Int, y: Int)(
      using s: Suspend): Int =
    println("Hello")
    val z = 1
    s.shift[Int] { continuation => continuation.resume(x + y + z) }
  println(twoArgumentsSingleResumeContinuationsBeforeUsedInResume(1, 2))
