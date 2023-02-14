package examples

import continuations.Suspend

@main def TwoCurriedArgumentsOneContinuations =
  def twoCurriedArgumentsOneContinuations(x: Int)(y: Int)(using Suspend): Int =
    summon[Suspend].suspendContinuation[Int] { continuation =>
      continuation.resume(Right(x + y + 1))
    }
  println(twoCurriedArgumentsOneContinuations(1)(1))
