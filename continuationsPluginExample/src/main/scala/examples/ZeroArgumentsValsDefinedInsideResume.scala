package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedInsideResume =
  def zeroArgumentsValsDefinedInsideResume()(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume {
        val x = 1
        Right(println(x))
      }
    }

    summon[Suspend].shift[Int] { continuation =>
      continuation.resume {
        val x = 2
        Right(x)
      }
    }
    10
  println(zeroArgumentsValsDefinedInsideResume())
