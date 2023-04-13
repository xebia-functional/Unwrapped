package examples

import continuations.Suspend

@main def OneFunctionArgumentTwoContinuationsTwoResumes =
  def oneFunctionArgumentTwoContinuationsTwoResumes(f: Int => Int)(using s: Suspend): Int =
    shift(_.resume(println(f(1))))
    shift(_.resume(f(2)))
  println(oneFunctionArgumentTwoContinuationsTwoResumes(_ + 1))
