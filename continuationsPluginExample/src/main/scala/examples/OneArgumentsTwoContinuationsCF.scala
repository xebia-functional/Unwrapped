package examples

import continuations.Suspend

@main def OneArgumentsTwoContinuationsCF =
  def oneArgumentsTwoContinuationsCF(x: Int): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  println(oneArgumentsTwoContinuationsCF(1))
