package examples

import continuations.Suspend

import scala.util.Try

@main def ZeroArgumentsTwoContinuationsMultipleResume =
  def zeroArgumentsTwoContinuationsMultipleResume()(using Suspend): Int =
    shift[Int] { c => c.resume({ println("Resume1"); 1 }) }
    shift[Int] { c =>
      c.resume({ println("Resume2"); 1 })
      c.resume({ println("Resume3"); 2 })
    }
  println(Try(zeroArgumentsTwoContinuationsMultipleResume()))
