package examples

import continuations.Suspend

@main def ZeroArgumentsCodeAfter =
  def zeroArgumentsCodeAfter()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(println(1))
    }
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(println(2))
    }
    3
  println(zeroArgumentsCodeAfter())
