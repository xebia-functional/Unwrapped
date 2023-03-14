package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedInsideResume =
  def zeroArgumentsValsDefinedInsideResume()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume {
        val x = 1
        println(x)
      }
    }

    summon[Suspend].suspendContinuation[Int] { continuation =>
      continuation.resume {
        val x = 2
        x
      }
    }
    10
  println(zeroArgumentsValsDefinedInsideResume())
