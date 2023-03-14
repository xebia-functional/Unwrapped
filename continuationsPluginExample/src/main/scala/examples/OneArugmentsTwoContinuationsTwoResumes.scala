package examples

import continuations.Suspend

@main def OneFunctionArgumentTwoContinuationsTwoResumes =
  def oneFunctionArgumentTwoContinuationsTwoResumes(f: Int => Int)(using s: Suspend): Int =
    s.suspendContinuation(_.resume(println(f(1))))
    s.suspendContinuation(_.resume(f(2)))
  println(oneFunctionArgumentTwoContinuationsTwoResumes(_ + 1))
