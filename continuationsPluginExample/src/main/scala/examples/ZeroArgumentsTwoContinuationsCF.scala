package examples

import continuations.Suspend

@main def ZeroArgumentsTwoContinuationsCF =
  def zeroArgumentsTwoContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(println(1))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(2) }
  println(zeroArgumentsTwoContinuationsCF())
