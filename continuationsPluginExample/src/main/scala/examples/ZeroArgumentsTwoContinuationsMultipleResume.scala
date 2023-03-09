package examples

import continuations.Suspend

import scala.util.Try

@main def ZeroArgumentsTwoContinuationsMultipleResume =
  def zeroArgumentsTwoContinuationsMultipleResume()(using s: Suspend): Int =
    s.shift[Int] { c => c.resume(Right { println("Resume1"); 1 }) }
    s.shift[Int] { c =>
      c.resume(Right {
        println("Resume2"); 1
      })
      c.resume(Right {
        println("Resume3"); 2
      })
    }
  println(Try(zeroArgumentsTwoContinuationsMultipleResume()))
