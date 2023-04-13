package examples

import continuations.Suspend

@main def OneArgumentsTwoContinuations =
  def oneArgumentsTwoContinuations(x: Int)(using s: Suspend): Int =
    shift[Unit] { continuation => continuation.resume(println(x)) }
    shift[Int] { continuation => continuation.resume(1) }
  println(oneArgumentsTwoContinuations(1))
