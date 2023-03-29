package examples

import continuations.Suspend


// val foo:  Suspend ?=> [A] => List[A] => Int = [A] => (list: List[A]) => list.size

def threeDependentContinuations(a: Int, b: Int, c: Int)(using s: Suspend): Int =
  val d = 4
  val continuationOne: Int = s.shift(_.resume(d + a)) // 5
  val e = 5
  val continuationTwo: Int =
    s.shift(_.resume(continuationOne + e + b)) // 12
  val f = 6
  val result: Int = s.shift(_.resume(continuationTwo + f + c)) // 21
  result



// val polymorphicValDefZeroContinuations
//     : Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int =
//   [A] =>
//     (x: List[A]) =>
//       [B] =>
//         (y: List[B]) =>
//           (q: List[A]) =>
//             (p: List[A]) =>

//               val z = 1
//               x.size + y.size + q.size + p.size + z

@main def main =
  // println(foo(List(1)))
  println(threeDependentContinuations(1, 2, 3))
  // println(
  //   polymorphicValDefZeroContinuations(List(1))(List("A", "B"))(List(1, 1, 1))(
  //     List(1, 1, 1, 1)))
