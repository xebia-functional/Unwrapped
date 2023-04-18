package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def MultipleSuspendWithExpressionsInBody =
  def foo1()(using s: Suspend): Int =
    println("Start")
    s.shift[Unit] { _.resume(println("Hello")) }
    println("World")
    val x = 1
    s.shift[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume({ println(q); false })
    }
    10

  println(SuspendApp(foo1()))
