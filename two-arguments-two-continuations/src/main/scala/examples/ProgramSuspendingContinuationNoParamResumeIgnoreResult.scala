package examples

import continuations.*

@main def ProgramSuspendingContinuationNoParamResumeIgnoreResult =
  def fooTest()(using Suspend): Int =
    println("Start")
    shift[Unit] { _.resume({ println("Hello") }) }
    println("World")
    val x = 1
    shift[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume({ println(q); false })
    }
    shift[Int] { continuation =>
      continuation.raise(new Exception("error"))
    }
    10
  fooTest()
