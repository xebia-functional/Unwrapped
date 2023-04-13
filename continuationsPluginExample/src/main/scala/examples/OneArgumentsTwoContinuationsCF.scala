package examples

import continuations.Suspend

@main def OneArgumentsTwoContinuationsCF =
  def oneArgumentsTwoContinuationsCF(x: Int): Suspend ?=> Int =
    shift[Unit] { continuation => continuation.resume(println(x)) }
    shift[Int] { continuation => continuation.resume(2) }
  println(oneArgumentsTwoContinuationsCF(1))
