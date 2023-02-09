package examples

import continuations.Suspend

@main def OneArgumentsSingleResumeContinuationsBefore =
  def oneArgumentsSingleResumeContinuationsBefore(x: Int)(using Suspend): Int =
    println("Hello")
    val y = x
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
  println(oneArgumentsSingleResumeContinuationsBefore(1))
