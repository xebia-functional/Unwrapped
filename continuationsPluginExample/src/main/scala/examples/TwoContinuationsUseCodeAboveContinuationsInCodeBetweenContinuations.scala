package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoContinuationsUseCodeAboveContinuationsInCodeBetweenContinuations =
  def twoContinuationsUseCodeAboveContinuationsInCodeBetweenContinuations()(
      using s: Suspend): Int =
    val x = 1
    s.shift(_.resume(println("Resume 1")))
    val y = 1
    s.shift(_.resume(println(s"Resume 2: ${x + y}")))
    10
  println(SuspendApp(twoContinuationsUseCodeAboveContinuationsInCodeBetweenContinuations()))
