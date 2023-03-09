package examples

import continuations.Suspend

@main def OneArgumentsTwoContinuations =
  def oneArgumentsTwoContinuations(x: Int)(using s: Suspend): Int =
    s.shift[Unit] { continuation => continuation.resume(Right(println(x))) }
    s.shift[Int] { continuation => continuation.resume(Right(1)) }
  println(oneArgumentsTwoContinuations(1))
