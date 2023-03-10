package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedInsideContinuation =
  def zeroArgumentsValsDefinedInsideContinuation()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      val x = 1
      continuation.resume(println(x))
    }
    val x = 1
    summon[Suspend].suspendContinuation[Int] { continuation =>
      val x = 2
      continuation.resume(x)
    }
  println(zeroArgumentsValsDefinedInsideContinuation())
