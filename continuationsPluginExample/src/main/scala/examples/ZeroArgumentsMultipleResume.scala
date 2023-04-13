package examples

import continuations.Suspend

import scala.util.Try

@main def ZeroArgumentsMultipleResume =
  def zeroArgumentsMultipleResume()(using Suspend): Int =
    shift[Int] { c =>
      c.resume( { println("Resume1"); 1 })
      c.resume( { println("Resume2"); 2 })
    }
  println(Try(zeroArgumentsMultipleResume()))
