package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneFunctionParameterOneResume =
  def oneFunctionParameterOneResume(f: Int => Int)(using s: Suspend): Int =
    s.shift(_.resume(f(1)))
  println(SuspendApp(oneFunctionParameterOneResume(_ + 1)))
