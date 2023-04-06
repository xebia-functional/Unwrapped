package examples

import continuations.Suspend


// val foo:  Suspend ?=> [A] => List[A] => Int = [A] => (list: List[A]) => list.size

def threeDependentContinuations[A](x: A)(using s: Suspend): A =
  s.shift(_.resume(x)) // 5

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
  println(threeDependentContinuations(1))
  // println(
  //   polymorphicValDefZeroContinuations(List(1))(List("A", "B"))(List(1, 1, 1))(
  //     List(1, 1, 1, 1)))
