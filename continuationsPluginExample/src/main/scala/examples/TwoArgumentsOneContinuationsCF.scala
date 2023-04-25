package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsOneContinuationsCF =
  def twoArgumentsOneContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
    summon[Suspend].shift[Int](_.resume(x + y + 1))
  println(SuspendApp(twoArgumentsOneContinuationsCF(1, 1)))
