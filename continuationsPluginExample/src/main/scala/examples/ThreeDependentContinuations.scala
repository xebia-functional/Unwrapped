package examples

import continuations.*

@main def ThreeDependentContinuations =
  def threeDependentContinuations(a: Int, b: Int, c: Int)(using s: Suspend): Int =
    val d = 4
    val continuationOne: Int = s.suspendContinuation(_.resume(d + a))
    val e = 5
    val continuationTwo: Int = s.suspendContinuation(_.resume(continuationOne + e + b))
    val f = 6
    val result: Int = s.suspendContinuation(_.resume(continuationTwo + f + c))
    result
  println(threeDependentContinuations(1, 2, 3))
