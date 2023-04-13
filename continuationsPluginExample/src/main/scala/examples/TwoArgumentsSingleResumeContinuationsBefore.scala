package examples

import continuations.Suspend

@main def TwoArgumentsSingleResumeContinuationsBefore =
  def twoArgumentsSingleResumeContinuationsBefore(x: Int, y: Int)(using Suspend): Int =
    println("Hello")
    val z = x + y
    shift(_.resume(1))
  println(twoArgumentsSingleResumeContinuationsBefore(1, 2))
