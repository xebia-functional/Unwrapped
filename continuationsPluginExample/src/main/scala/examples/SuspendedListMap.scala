package examples

import continuations.Suspend

@main def SuspendedListMap =
  def suspendMap[A, B](xs: List[A], f: A => Suspend ?=> B)(using Suspend): List[B] =
    xs match {
      case Nil => Nil
      case y :: ys => f(y) :: suspendMap(ys, f)
    }
  def suspendIncrement(x: Int): Suspend ?=> Int =
    val z = 1
    summon[Suspend].shift[Int](_.resume(x + z))
  val mappedContinuations = suspendMap(List(1, 2, 3, 4), suspendIncrement)
  println(mappedContinuations)
