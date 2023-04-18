package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoArgumentsTwoDependentContinuations =
  def twoArgumentsTwoDependentContinuations(x: Int, y: Int)(using s: Suspend): Int = {
    val z = s.shift[Int](_.resume(x + y + 1))
    s.shift[Int](_.resume(z + 1))
  }

  println(SuspendApp(twoArgumentsTwoDependentContinuations(1, 1)))
