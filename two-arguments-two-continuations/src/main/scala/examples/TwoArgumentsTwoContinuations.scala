package examples

import continuations.Suspend
import scala.util.Try

@main def main =
  // val fooTest: Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int =
  //   [A] =>
  //     (x: List[A]) =>
  //       [B] =>
  //         (y: List[B]) =>
  //           (q: List[A]) =>
  //             (p: List[A]) =>
  //               val z = 1
  //               x.size + y.size + q.size + p.size + z

  // println(fooTest(List(1))(List("A", "B"))(List(1, 1, 1))(List(1, 1, 1, 1)))
  def fooTest(qq: Int)(using s: Suspend): Int =
    val pp = 11
    val xx = s.shift[Int] { _.resume(qq - 1) }
    val ww = 13
    val rr = "AAA"
    val yy = s.shift[String] { c => c.resume(rr) }
    val tt = 100
    val zz = s.shift[Int] { _.resume(ww - 1 + xx) }
    println(xx)
    xx + qq + zz + pp + tt + yy.length

  fooTest(12)
