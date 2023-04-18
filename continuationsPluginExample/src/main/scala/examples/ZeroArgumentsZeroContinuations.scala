package examples

import continuations.*
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsZeroContinuations =
  def zeroArgumentsZeroContinuations()(using Suspend): Int = 1
  println(SuspendApp(zeroArgumentsZeroContinuations()))
