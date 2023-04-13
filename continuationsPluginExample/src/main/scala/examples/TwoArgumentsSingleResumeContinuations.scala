package examples

import continuations.Suspend

@main def TwoArgumentsSingleResumeContinuations =
  def twoArgumentsSingleResumeContinuations(x: Int, y: Int)(using Suspend): Int =
    shift[Int] { continuation => continuation.resume(x + y + 1) }
  println(twoArgumentsSingleResumeContinuations(1, 2))
