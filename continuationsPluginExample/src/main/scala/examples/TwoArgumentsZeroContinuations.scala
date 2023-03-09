package examples

import continuations.Suspend

@main def TwoArgumentsZeroContinuations =
  def twoArgumentsZeroContinuations(x: Int, y: Int)(using Suspend): Int = x + y + 1
  println(twoArgumentsZeroContinuations(1, 1))
