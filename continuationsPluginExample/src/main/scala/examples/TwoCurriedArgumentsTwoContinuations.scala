package examples

import continuations.Suspend

@main def TwoCurriedArgumentsTwoContinuations =
  def twoCurriedArgumentsTwoContinuations(x: Int)(y: Int)(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(3)) }
  println(twoCurriedArgumentsTwoContinuations(1)(2))
