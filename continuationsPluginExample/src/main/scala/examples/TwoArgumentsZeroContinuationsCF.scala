package examples

import continuations.Suspend

@main def TwoArgumentsZeroContinuationsCF =
  def twoArgumentsZeroContinuationsCF(x: Int, y: Int): Suspend ?=> Int = x + y + 1
  println(twoArgumentsZeroContinuationsCF(1, 2))
