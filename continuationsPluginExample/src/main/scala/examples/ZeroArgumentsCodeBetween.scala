package examples

import continuations.Suspend

@main def ZeroArgumentsCodeBetween =
  def zeroArgumentsCodeBetween()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(println(1))
    }
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(2) }
  println(zeroArgumentsCodeBetween())
