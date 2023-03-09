package examples

import continuations.Suspend

@main def OneArgumentsTwoContinuationsCF =
  def oneArgumentsTwoContinuationsCF(x: Int): Suspend ?=> Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(Right(2)) }
  println(oneArgumentsTwoContinuationsCF(1))
