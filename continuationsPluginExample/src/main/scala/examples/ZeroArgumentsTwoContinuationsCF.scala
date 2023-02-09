package examples

import continuations.Suspend

@main def ZeroArgumentsTwoContinuationsCF =
  def zeroArgumentsTwoContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  println(zeroArgumentsTwoContinuationsCF())
