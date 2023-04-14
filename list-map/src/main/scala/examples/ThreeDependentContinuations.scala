package examples

import continuations.*
import continuations.jvm.internal.{SuspendApp, Starter}

@main def ThreeDependentContinuations(): Unit =
  def threeDependentContinuations(a: Int, b: Int, c: Int)(using s: Suspend): Int =
    val d = 4
    val continuationOne: Int = s.shift[Int] { c2 =>
      println(s"${Thread.currentThread().getName}, continuation 1, shift 1")
      c2.resume({
        println(s"${Thread.currentThread().getName}, resume 1 continuation 1")
        d + a
      })
    }// 5
    val e = 5
    val continuationTwo: Int =
      s.shift[Int] { c2 =>
        println(s"${Thread.currentThread().getName}, continuation 2, shift 2")
        c2.resume ({
          println(s"${Thread.currentThread().getName}, resume 2 continuation 2")
          continuationOne + e + b
        })} // 12
    val f = 6
    val result: Int = s.shift[Int] { c2 =>
      println(s"${Thread.currentThread().getName}, continuation 3, shift 3")
      c2.resume({
        println(s"${Thread.currentThread().getName}, resume 2 continuation 2")
        continuationTwo + f + c
      })
    }
    result


  println(SuspendApp(threeDependentContinuations(1,2,3)))
