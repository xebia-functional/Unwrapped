package examples

import continuations.*

@main def ThreeDependentContinuations =
  def threeDependentContinuations(a: Int, b: Int, c: Int)(using s: Suspend): Int =
    val d = 4
    val continuationOne: Int = s.shift(_.resume(Right(d + a)))
    val e = 5
    val continuationTwo: Int = s.shift(_.resume(Right(continuationOne + e + b)))
    val f = 6
    val result: Int = s.shift(_.resume(Right(continuationTwo + f + c)))
    result
  println(threeDependentContinuations(1, 2, 3))
