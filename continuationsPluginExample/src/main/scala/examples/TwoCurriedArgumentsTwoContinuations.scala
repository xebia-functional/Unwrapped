package examples

import continuations.Suspend

@main def TwoCurriedArgumentsTwoContinuations =
  def twoCurriedArgumentsTwoContinuations(x: Int)(y: Int)(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x + y))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(3) }
  println(twoCurriedArgumentsTwoContinuations(1)(2))
