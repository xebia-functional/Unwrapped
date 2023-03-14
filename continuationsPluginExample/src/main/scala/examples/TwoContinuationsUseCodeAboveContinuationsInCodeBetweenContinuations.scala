package examples

import continuations.Suspend

@main def TwoContinuationsUseCodeAboveContinuationsInCodeBetweenContinuations =
  def twoContinuationsUseCodeAboveContinuationsInCodeBetweenContinuations()(
      using s: Suspend): Int =
    val x = 1
    s.suspendContinuation(_.resume(println("Resume 1")))
    val y = 1
    s.suspendContinuation(_.resume(println(s"Resume 2: ${x + y}")))
    10
  println(twoContinuationsUseCodeAboveContinuationsInCodeBetweenContinuations())
