package examples

import continuations.Suspend

@main def ZeroArgumentsCodeBefore =
  def zeroArgumentsCodeBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    shift[Unit](continuation => continuation.resume(println(1)))
    shift[Int](continuation => continuation.resume(2))
  println(zeroArgumentsCodeBefore())
