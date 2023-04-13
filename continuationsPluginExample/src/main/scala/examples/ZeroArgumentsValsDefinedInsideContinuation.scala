package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedInsideContinuation =
  def zeroArgumentsValsDefinedInsideContinuation()(using Suspend): Int =
    shift[Unit] { continuation =>
      val x = 1
      continuation.resume(println(x))
    }
    val x = 1
    shift[Int] { continuation =>
      val x = 2
      continuation.resume(x)
    }
  println(zeroArgumentsValsDefinedInsideContinuation())
