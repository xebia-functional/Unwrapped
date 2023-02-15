package examples

import continuations.Suspend

@main def TwoArgumentsTwoDependentContinuations =
  def twoArgumentsTwoDependentContinuations(x: Int, y: Int)(using s: Suspend): Int = {
    val z = s.suspendContinuation[Int] {
      _.resume(Right { x + y + 1 })
    }
    s.suspendContinuation[Int] {
      _.resume(Right { z + 1 })
    }
  }

  println(twoArgumentsTwoDependentContinuations(1, 1))
