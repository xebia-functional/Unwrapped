package examples

import continuations.Suspend

@main def OneArgumentsTwoContinuations =
  def oneArgumentsTwoContinuations(x: Int)(using s: Suspend): Int =
    s.suspendContinuation[Unit] { continuation => continuation.resume(println(x)) }
    s.suspendContinuation[Int] { continuation => continuation.resume(1) }
  println(oneArgumentsTwoContinuations(1))
