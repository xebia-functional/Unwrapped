package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentOneCurriedArgumentZeroContinuations =
  def oneArgumentOneCurriedArgumentZeroContinuations(x: Int)(y: Int)(using Suspend): Int =
    x + y + 1
  println(SuspendApp(oneArgumentOneCurriedArgumentZeroContinuations(1)(1)))
