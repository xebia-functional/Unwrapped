package examples

import continuations.Suspend

import scala.util.Try

@main def ZeroArgumentsMultipleResume =
  def zeroArgumentsMultipleResume()(using s: Suspend): Int =
    s.suspendContinuation[Int] { c =>
      c.resume(Right { println("Resume1"); 1 })
      c.resume(Right { println("Resume2"); 2 })
    }
  println(Try(zeroArgumentsMultipleResume()))
