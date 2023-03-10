package examples

import continuations.Suspend

@main def UseValsDefinedInsideContinuation =
  def useValsDefinedInsideContinuation()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Int] { continuation =>
      val x = 1
      val y = 2
      x + y
      continuation.resume(3)
    }
  println(useValsDefinedInsideContinuation())
