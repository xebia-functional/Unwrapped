package examples

import continuations.Suspend

import scala.util.Try

def programOneContinuationReturnValue: Int =
  def zeroArgumentsSingleResumeContinuationsBeforeAfter()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    2
  zeroArgumentsSingleResumeContinuationsBeforeAfter()

def demoPrints =
  case class Foo(i: Int)
  given String = "Output: "

  programOneContinuationReturnValue
