package examples

import continuations.Suspend

@main def TwoArgumentsTwoContinuations =
  def twoArgumentsTwoContinuations(x: Int, y: Int)(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x + y))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
  println(twoArgumentsTwoContinuations(1, 2))
