package examples

import continuations.Suspend

@main def ResumeWithValsInsideTheContinuation =
  def resumeWithValsInsideTheContinuation()(using s: Suspend): Int =
    s.shift[Int] { continuation =>
      val x = 1
      val y = 2
      continuation.resume(x + y)
    }
  println(resumeWithValsInsideTheContinuation())
