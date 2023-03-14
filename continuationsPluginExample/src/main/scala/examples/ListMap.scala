package examples

import continuations.Suspend

@main def ListMap =
  def twoArgumentsOneContinuationsCFBefore(x: Int, y: Int): Suspend ?=> Int =
    val z = 1
    summon[Suspend].suspendContinuation[Int] { continuation =>
      continuation.resume(x + y + z)
    }
  val mappedContinuations = List(1, 2, 3, 4).map(twoArgumentsOneContinuationsCFBefore(1, _))
  println(mappedContinuations)
