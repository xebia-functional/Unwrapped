package examples

import continuations.Suspend

@main def ZeroArgumentsOneContinuationsCF =
  def zeroArgumentsOneContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
  println(zeroArgumentsOneContinuationsCF())
