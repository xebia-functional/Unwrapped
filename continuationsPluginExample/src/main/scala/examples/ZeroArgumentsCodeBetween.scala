package examples

import continuations.Suspend

@main def ZeroArgumentsCodeBetween =
  def zeroArgumentsCodeBetween()(using Suspend): Int =
    shift[Unit] { continuation =>
      continuation.resume(println(1))
    }
    println("Hello")
    val x = 1
    shift[Int] { continuation => continuation.resume(2) }
  println(zeroArgumentsCodeBetween())
