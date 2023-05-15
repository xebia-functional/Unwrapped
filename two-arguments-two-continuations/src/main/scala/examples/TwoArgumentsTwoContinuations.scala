package examples

import continuations.Suspend
import continuations.Blocking
import continuations.Continuation

@main def TwoArgumentsTwoContinuations =
  def twoArgumentsTwoContinuations(x: Int, y: Int)(using Suspend): Int =
    val z = summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x + y))
    }
    summon[Suspend].shift[Int] { continuation =>
      println(s"woot")
      continuation.resume(x+y)
    }
    
  println(Blocking(twoArgumentsTwoContinuations(1, 2)))

