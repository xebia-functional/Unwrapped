package examples

import continuations.Suspend
import scala.util.Try

@main def ProgramMultipleSuspendLeft =
  def foo()(using s: Suspend): Int =
    s.suspendContinuation[Int] { _.resume(Left(new Exception("error"))) }
    s.suspendContinuation[Int] { _.resume(Right { println("Resume2"); 2 }) }
  println(Try(foo()))
