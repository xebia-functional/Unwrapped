package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

import scala.util.Try

@main def ProgramMultipleSuspendLeft =
  def foo()(using s: Suspend): Int =
    s.shift[Int] { _.raise(new Exception("error")) }
    s.shift[Int] { _.resume({ println("Resume2"); 2 }) }
  println(Try(SuspendApp(foo())))
