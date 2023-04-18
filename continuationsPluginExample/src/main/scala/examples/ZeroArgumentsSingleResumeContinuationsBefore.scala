package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsSingleResumeContinuationsBefore =
  def zeroArgumentsSingleResumeContinuationsBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
  println(SuspendApp(zeroArgumentsSingleResumeContinuationsBefore()))
