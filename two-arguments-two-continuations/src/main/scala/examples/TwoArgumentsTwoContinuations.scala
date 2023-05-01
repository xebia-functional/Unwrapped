package examples

import continuations.Suspend
import continuations.Blocking
import continuations.Continuation

@main def TwoArgumentsTwoContinuations =
  def twoArgumentsTwoContinuations(x: Int, y: Int)(using Suspend): Int =
    val z = summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x + y))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
    
  // println(Blocking(twoArgumentsTwoContinuations(1, 2)))

