package examples

import continuations.Suspend

@main def OneArgumentsSingleResumeContinuations =
  def oneArgumentsSingleResumeContinuations(x: Int)(using Suspend): Int =
    shift[Int] { continuation => continuation.resume(x + 1) }
  println(oneArgumentsSingleResumeContinuations(1))
