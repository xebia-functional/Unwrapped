package examples

import continuations.Suspend

@main def OneArgumentsZeroContinuationsCF =
  def oneArgumentsZeroContinuationsCF(x: Int): Suspend ?=> Int = x + 1
  println(oneArgumentsZeroContinuationsCF(1))
