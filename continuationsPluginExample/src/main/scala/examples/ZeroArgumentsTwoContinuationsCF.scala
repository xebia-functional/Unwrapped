package examples

import continuations.Suspend

@main def ZeroArgumentsTwoContinuationsCF =
  def zeroArgumentsTwoContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(1))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(2) }
  println(zeroArgumentsTwoContinuationsCF())
