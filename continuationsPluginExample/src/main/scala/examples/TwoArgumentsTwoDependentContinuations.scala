package examples

import continuations.Suspend

@main def TwoArgumentsTwoDependentContinuations =
  def twoArgumentsTwoDependentContinuations(x: Int, y: Int)(using s: Suspend): Int = {
    val z = shift[Int](_.resume(x + y + 1))
    shift[Int](_.resume(z + 1))
  }

  println(twoArgumentsTwoDependentContinuations(1, 1))
