package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def PolymorphicDefsZeroContinuations: Unit =
  val polymorphicValDefZeroContinuations
      : Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int =
    [A] =>
      (x: List[A]) =>
        [B] =>
          (y: List[B]) =>
            (q: List[A]) =>
              (p: List[A]) =>

                val z = 1
                x.size + y.size + q.size + p.size + z

  def polymorphicDefDefZeroContinuations
      : Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int =
    [A] =>
      (x: List[A]) =>
        [B] =>
          (y: List[B]) =>
            (q: List[A]) =>
              (p: List[A]) =>

                val z = 1
                x.size + y.size + q.size + p.size + z

  println(
    SuspendApp(polymorphicValDefZeroContinuations(List(1))(List("A", "B"))(List(1, 1, 1))(
      List(1, 1, 1, 1))))

  println(
    SuspendApp(polymorphicDefDefZeroContinuations(List(1))(List("A", "B"))(List(1, 1, 1))(
      List(1, 1, 1, 1))))
