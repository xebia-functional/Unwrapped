package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedAboveContinuation =
  def zeroArgumentsValsDefinedAboveContinuation()(using Suspend): Int =
    println("Hello")
    val x = 1
    shift[Unit] { continuation => continuation.resume(println(x)) }
    val y = 2
    shift[Int] { continuation => continuation.resume(y) }
  println(zeroArgumentsValsDefinedAboveContinuation())
