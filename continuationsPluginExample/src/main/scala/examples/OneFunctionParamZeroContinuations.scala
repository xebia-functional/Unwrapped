package examples

import continuations.Suspend

@main def OneFunctionParamZeroContinuations =
  def oneFunctionParamZeroContinuations(f: Int => Int)(using Suspend): Int = f(1)
  println(oneFunctionParamZeroContinuations(_ + 1))
