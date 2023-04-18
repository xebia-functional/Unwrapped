package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsTwoContinuationsCF =
  def oneArgumentsTwoContinuationsCF(x: Int): Suspend ?=> Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(2) }
  println(SuspendApp(oneArgumentsTwoContinuationsCF(1)))
