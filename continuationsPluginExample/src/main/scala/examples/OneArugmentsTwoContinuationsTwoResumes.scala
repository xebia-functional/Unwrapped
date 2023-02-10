package examples

import continuations.Suspend

@main def OneFunctionArgumentTwoContinuationsTwoResumes =
  def oneFunctionArgumentTwoContinuationsTwoResumes(f: Int => Int)(using s: Suspend): Int =
    s.suspendContinuation(_.resume(Right(println(f(1)))))
    s.suspendContinuation(_.resume(Right(f(2))))
  println(oneFunctionArgumentTwoContinuationsTwoResumes(_ + 1))
