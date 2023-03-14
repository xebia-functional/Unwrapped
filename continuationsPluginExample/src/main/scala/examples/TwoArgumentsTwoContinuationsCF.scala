package examples

import continuations.Suspend

@main def TwoArgumentsTwoContinuationsCF =
  def twoArgumentsTwoContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(println(x + y))
    }
    summon[Suspend].suspendContinuation[Int](continuation => continuation.resume(3))
  println(twoArgumentsTwoContinuationsCF(1, 2))
