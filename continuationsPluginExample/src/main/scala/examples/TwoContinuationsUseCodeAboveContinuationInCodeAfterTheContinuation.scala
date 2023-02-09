package examples

import continuations.Suspend

@main def TwoContinuationsUseCodeAboveContinuationInCodeAfterTheContinuation =
  def twoContinuationsUseCodeAboveContinuationInCodeAfterTheContinuation()(
      using s: Suspend): Int =
    println("Hello")
    val x = 1
    s.suspendContinuation(_.resume(Right(println("Resume 1"))))
    s.suspendContinuation(_.resume(Right(println(s"Resume 2 $x"))))
    x + 1
  println(twoContinuationsUseCodeAboveContinuationInCodeAfterTheContinuation())
