package examples

import continuations.Suspend

@main def ZeroArgumentsZeroContinuationsCF =
  def zeroArgumentsZeroContinuationsCF(): Suspend ?=> Int = 1
  println(zeroArgumentsZeroContinuationsCF())
