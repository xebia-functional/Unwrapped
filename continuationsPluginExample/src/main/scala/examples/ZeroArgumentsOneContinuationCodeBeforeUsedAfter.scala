package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsOneContinuationCodeBeforeUsedAfter =
  def zeroArgumentsOneContinuationCodeBeforeUsedAfter()(using s: Suspend): Int =
    val x = 1
    s.shift[Unit](_.resume(println("Hello")))
    val y = 1
    x + y
  println(SuspendApp(zeroArgumentsOneContinuationCodeBeforeUsedAfter()))
