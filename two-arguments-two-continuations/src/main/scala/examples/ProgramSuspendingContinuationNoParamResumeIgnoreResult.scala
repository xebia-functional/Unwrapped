package examples

import continuations.*

@main def ProgramSuspendingContinuationNoParamResumeIgnoreResult =
  def fooTest()(using s: Suspend): Int =
    println("Start")
    s.shift[Unit] { _.resume(Right { println("Hello") }) }
    println("World")
    val x = 1
    s.shift[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume(Right { println(q); false })
    }
    s.shift[Int] { _.resume(Left(new Exception("error"))) }
    10
  fooTest()
