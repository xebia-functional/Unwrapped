package examples

import continuations.Suspend

@main def OneArgumentsZeroContinuations =
  def oneArgumentsZeroContinuations(x: Int)(using Suspend): Int = x + 1
  println(oneArgumentsZeroContinuations(1))
