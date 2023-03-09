package examples

import continuations.Suspend

@main def ZeroArgumentsCodeBefore =
  def zeroArgumentsCodeBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(Right(2)) }
  println(zeroArgumentsCodeBefore())
