package examples

import continuations.Suspend

@main def OneFunctionArgumentTwoContinuationsTwoResumes =
  def oneFunctionArgumentTwoContinuationsTwoResumes(f: Int => Int)(using s: Suspend): Int =
    s.shift(_.resume(println(f(1))))
    s.shift(_.resume(f(2)))
  println(oneFunctionArgumentTwoContinuationsTwoResumes(_ + 1))
