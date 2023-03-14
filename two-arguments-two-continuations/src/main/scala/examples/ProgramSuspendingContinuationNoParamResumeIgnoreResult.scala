package examples

import continuations.*

@main def ProgramSuspendingContinuationNoParamResumeIgnoreResult =
  def fooTest()(using s: Suspend): Int =
    println("Start")
    s.shift[Unit] { _.resume({ println("Hello") }) }
    println("World")
    val x = 1
    s.shift[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume({ println(q); false })
    }
    s.shift[Int] { continuation =>
      continuation.raise(new Exception("error"))
    }
    10
  fooTest()
