package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsValsDefinedInsideContinuation =
  def zeroArgumentsValsDefinedInsideContinuation()(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      val x = 1
      continuation.resume(println(x))
    }
    val x = 1
    summon[Suspend].shift[Int] { continuation =>
      val x = 2
      continuation.resume(x)
    }
  println(SuspendApp(zeroArgumentsValsDefinedInsideContinuation()))
