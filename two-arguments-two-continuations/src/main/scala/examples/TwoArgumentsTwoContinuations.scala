package examples

import continuations.Suspend
import continuations.Blocking
import continuations.Continuation

@main def TwoArgumentsTwoContinuations =
  given String = "hi"
  given Int = 42
  def threeDependentContinuations(x: Int)(using i: Int)(using s: Suspend, strung: String): Int =
    s.shift(_.resume(1))
  println(Blocking(threeDependentContinuations(1)))

