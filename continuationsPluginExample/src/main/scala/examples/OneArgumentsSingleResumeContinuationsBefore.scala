package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsSingleResumeContinuationsBefore =
  def oneArgumentsSingleResumeContinuationsBefore(x: Int)(using Suspend): Int =
    println("Hello")
    val y = x
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
  println(SuspendApp(oneArgumentsSingleResumeContinuationsBefore(1)))
