package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsZeroContinuationsCF =
  def oneArgumentsZeroContinuationsCF(x: Int): Suspend ?=> Int = x + 1
  println(SuspendApp(oneArgumentsZeroContinuationsCF(1)))
