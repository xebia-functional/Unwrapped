package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsZeroContinuations =
  def oneArgumentsZeroContinuations(x: Int)(using Suspend): Int = x + 1
  println(SuspendApp(oneArgumentsZeroContinuations(1)))
