package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsZeroContinuationsCF =
  def twoArgumentsZeroContinuationsCF(x: Int, y: Int): Suspend ?=> Int = x + y + 1
  println(SuspendApp(twoArgumentsZeroContinuationsCF(1, 2)))
