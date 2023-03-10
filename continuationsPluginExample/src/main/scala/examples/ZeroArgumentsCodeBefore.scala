package examples

import continuations.Suspend

@main def ZeroArgumentsCodeBefore =
  def zeroArgumentsCodeBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(println(1))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(2) }
  println(zeroArgumentsCodeBefore())
