package examples

import continuations.Suspend

@main def TwoCurriedArgumentsOneContinuations =
  def twoCurriedArgumentsOneContinuations(x: Int)(y: Int)(using Suspend): Int =
    shift[Int](continuation => continuation.resume(x + y + 1))
  println(twoCurriedArgumentsOneContinuations(1)(1))
