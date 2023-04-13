package examples

import continuations.Suspend

@main def OneArgumentsSingleResumeContinuationsBefore =
  def oneArgumentsSingleResumeContinuationsBefore(x: Int)(using Suspend): Int =
    println("Hello")
    val y = x
    shift[Int] { continuation => continuation.resume(1) }
  println(oneArgumentsSingleResumeContinuationsBefore(1))
