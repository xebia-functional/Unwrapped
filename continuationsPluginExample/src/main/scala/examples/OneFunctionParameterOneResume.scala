package examples

import continuations.Suspend

@main def OneFunctionParameterOneResume =
  def foo(f: Int => Int)(using s: Suspend): Int =
    s.suspendContinuation(_.resume(Right(f(1))))
  println(foo(_ + 1))
