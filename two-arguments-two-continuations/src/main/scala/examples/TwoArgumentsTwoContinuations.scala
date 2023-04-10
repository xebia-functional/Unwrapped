package examples

import continuations.Suspend
import scala.util.Try

@main def main =
  def twoArgumentsOneContinuationsCFBefore(x: Int, y: Int): Suspend ?=> Int =
    val z = 1
    summon[Suspend].shift[Int] { continuation =>
      continuation.resume(x + y + z)
    }
  val mappedContinuations = List(1, 2, 3, 4).map(twoArgumentsOneContinuationsCFBefore(1, _))
  println(mappedContinuations)
