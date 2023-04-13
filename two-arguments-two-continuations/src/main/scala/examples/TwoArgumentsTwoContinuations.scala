package examples

import continuations.Suspend

@main def TwoArgumentsTwoContinuations =
  def twoArgumentsTwoContinuations(x: Int, y: Int)(using Suspend): Int =
    shift[Unit] { continuation => continuation.resume(println(x + y)) }
    shift[Int] { continuation => continuation.resume(1) }
  println(twoArgumentsTwoContinuations(1, 2))
