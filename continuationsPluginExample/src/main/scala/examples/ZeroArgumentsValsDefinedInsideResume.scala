package examples

import continuations.Suspend

@main def ZeroArgumentsValsDefinedInsideResume =
  def zeroArgumentsValsDefinedInsideResume()(using Suspend): Int =
    shift[Unit] { continuation =>
      continuation.resume {
        val x = 1
        println(x)
      }
    }

    shift[Int] { continuation =>
      continuation.resume {
        val x = 2
        x
      }
    }
    10
  println(zeroArgumentsValsDefinedInsideResume())
