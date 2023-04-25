package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsOneContinuationsCF =
  def oneArgumentsOneContinuationsCF(x: Int): Suspend ?=> Int =
    summon[Suspend].shift { _.resume(x + 1) }
  println(SuspendApp(oneArgumentsOneContinuationsCF(1)))
