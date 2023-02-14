package examples

import continuations.*

@main def ZeroArgumentsZeroContinuations =
  def zeroArgumentsZeroContinuations()(using Suspend): Int = 1
  println(zeroArgumentsZeroContinuations())
