package examples

import continuations.Suspend

@main def OneArgumentOneCurriedArgumentZeroContinuations =
  def oneArgumentOneCurriedArgumentZeroContinuations(x: Int)(y: Int)(using Suspend): Int =
    x + y + 1
  println(oneArgumentOneCurriedArgumentZeroContinuations(1)(1))
