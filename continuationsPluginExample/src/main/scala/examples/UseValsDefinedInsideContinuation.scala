package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def UseValsDefinedInsideContinuation =
  def useValsDefinedInsideContinuation()(using Suspend): Int =
    summon[Suspend].shift[Int] { continuation =>
      val x = 1
      val y = 2
      x + y
      continuation.resume(3)
    }
  println(SuspendApp(useValsDefinedInsideContinuation()))
