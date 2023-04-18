package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneFunctionParamZeroContinuations =
  def oneFunctionParamZeroContinuations(f: Int => Int)(using Suspend): Int = f(1)
  println(SuspendApp(oneFunctionParamZeroContinuations(_ + 1)))
