package examples

import continuations.Suspend

@main def MultipleSuspendWithExpressionsInBody =
  def foo1()(using Suspend): Int =
    println("Start")
    shift[Unit] { _.resume(println("Hello")) }
    println("World")
    val x = 1
    shift[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume({ println(q); false })
    }
    10

  println(foo1())
