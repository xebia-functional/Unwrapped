package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsTwoContinuationsCF =
  def twoArgumentsTwoContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x + y))
    }
    summon[Suspend].shift[Int](continuation => continuation.resume(3))
  println(SuspendApp(twoArgumentsTwoContinuationsCF(1, 2)))
