package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

import scala.util.Try

@main def LeftContinuation: Unit =
  def left()(using s: Suspend): Int =
    s.shift[Int] { continuation =>
      continuation.raise(new Exception("error"))
    }
  println(Try(SuspendApp(left())))
