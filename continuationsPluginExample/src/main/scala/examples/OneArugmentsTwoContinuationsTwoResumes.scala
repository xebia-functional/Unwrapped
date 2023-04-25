package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneFunctionArgumentTwoContinuationsTwoResumes =
  def oneFunctionArgumentTwoContinuationsTwoResumes(f: Int => Int)(using s: Suspend): Int =
    s.shift(_.resume(println(f(1))))
    s.shift(_.resume(f(2)))
  println(SuspendApp(oneFunctionArgumentTwoContinuationsTwoResumes(_ + 1)))
