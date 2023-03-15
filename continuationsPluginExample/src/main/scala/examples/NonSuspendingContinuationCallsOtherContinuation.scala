package examples

import continuations.*

@main def NonSuspendingContinuationCallsOtherContinuation =
  def cont2(x: Int)(using s:Suspend): Int =
    val y = s.shift[Int](_.resume(x + 1))
    val z = s.shift[Int](_.resume(y + 1))
    y + z
  def cont1(x: Int)(using s:Suspend): Int = s.shift(c => c.resume(cont2(x + 2)))
  println(cont1(1))

