package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ListMap =
  def twoArgumentsOneContinuationsCFBefore(x: Int, y: Int): Suspend ?=> Int =
    val z = 1
    summon[Suspend].shift(_.resume(x + y + z))
  val mappedContinuations = List(1, 2, 3, 4).map { x =>
    SuspendApp(twoArgumentsOneContinuationsCFBefore(1, x))
  }
  println(mappedContinuations)
