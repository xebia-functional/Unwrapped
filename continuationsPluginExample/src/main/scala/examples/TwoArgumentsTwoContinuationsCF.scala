package examples

import continuations.Suspend

@main def TwoArgumentsTwoContinuationsCF =
  def twoArgumentsTwoContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
    shift[Unit] { continuation => continuation.resume(println(x + y)) }
    shift[Int](continuation => continuation.resume(3))
  println(twoArgumentsTwoContinuationsCF(1, 2))
