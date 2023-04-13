package examples

import continuations.Suspend

@main def ZeroArgumentsTwoContinuations =
  def zeroArgumentsTwoContinuations()(using Suspend): Int =
    shift[Unit] { continuation => continuation.resume(println(1)) }
    shift[Int] { continuation => continuation.resume(2) }
  println(zeroArgumentsTwoContinuations())
