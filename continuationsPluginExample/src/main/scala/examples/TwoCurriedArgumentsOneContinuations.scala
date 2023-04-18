package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def TwoCurriedArgumentsOneContinuations =
  def twoCurriedArgumentsOneContinuations(x: Int)(y: Int)(using Suspend): Int =
    summon[Suspend].shift[Int] { continuation =>
      continuation.resume(x + y + 1)
    }
  println(SuspendApp(twoCurriedArgumentsOneContinuations(1)(1)))
