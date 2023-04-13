package examples

import continuations.Suspend

@main def TwoArgumentsSingleResumeContinuationsBeforeUsedInResume =
  def twoArgumentsSingleResumeContinuationsBeforeUsedInResume(x: Int, y: Int)(
      using Suspend): Int =
    println("Hello")
    val z = 1
    shift[Int] { continuation => continuation.resume(x + y + z) }
  println(twoArgumentsSingleResumeContinuationsBeforeUsedInResume(1, 2))
