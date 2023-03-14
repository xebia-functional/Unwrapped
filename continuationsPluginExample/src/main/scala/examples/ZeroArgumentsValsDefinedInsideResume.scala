package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedInsideResume =
  def zeroArgumentsValsDefinedInsideResume()(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume {
        val x = 1
        println(x)
      }
    }

    summon[Suspend].shift[Int] { continuation =>
      continuation.resume {
        val x = 2
        x
      }
    }
    10
  println(zeroArgumentsValsDefinedInsideResume())
