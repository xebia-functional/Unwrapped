package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsZeroContinuations =
  def twoArgumentsZeroContinuations(x: Int, y: Int)(using Suspend): Int = x + y + 1
  println(SuspendApp(twoArgumentsZeroContinuations(1, 1)))
