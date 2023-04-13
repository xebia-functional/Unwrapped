package examples

import continuations.Suspend
import scala.util.Try

@main def ProgramMultipleSuspendLeft =
  def foo()(using s: Suspend): Int =
    shift[Int] { _.raise(new Exception("error")) }
    shift[Int] { _.resume({ println("Resume2"); 2 }) }
  println(Try(foo()))
