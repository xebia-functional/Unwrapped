package examples

import continuations.Suspend

@main def ZeroArgumentsSingleResumeContinuationsBefore =
  def zeroArgumentsSingleResumeContinuationsBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
  println(zeroArgumentsSingleResumeContinuationsBefore())
