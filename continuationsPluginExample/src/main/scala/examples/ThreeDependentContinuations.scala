package examples

import continuations.*
import continuations.jvm.internal.SuspendApp

@main def ThreeDependentContinuations =
  def threeDependentContinuations(a: Int, b: Int, c: Int)(using s: Suspend): Int =
    val d = 4
    val continuationOne: Int = s.shift(_.resume(d + a))
    val e = 5
    val continuationTwo: Int = s.shift(_.resume(continuationOne + e + b))
    val f = 6
    val result: Int = s.shift(_.resume(continuationTwo + f + c))
    result
  println(SuspendApp(threeDependentContinuations(1, 2, 3)))
