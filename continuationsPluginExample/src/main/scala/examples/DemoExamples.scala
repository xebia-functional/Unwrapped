package examples

import continuations.Suspend

import scala.util.Try

//no continuation
// case #1
def zeroArgumentsZeroContinuations()(using Suspend): Int = 1

// case #2
def oneArgumentsZeroContinuations(x: Int)(using Suspend): Int = x + 1

// case #3
def twoArgumentsZeroContinuations(x: Int, y: Int)(using Suspend): Int = x + y + 1

// case #5
def twoCurriedArgumentsZeroContinuations(x: Int)(y: Int)(using Suspend): Int = x + y + 1

// case #6
def oneArgumentOneAdditionalGivenArgumentZeroContinuations(
    x: Int)(using Suspend, String): String = summon[String] + x

// case #7
def genericArgumentsZeroContinuations[A](a: A)(using Suspend): A = a

// case #8
def zeroArgumentsZeroContinuationsCF(): Suspend ?=> Int = 1

// case #9
def oneArgumentsZeroContinuationsCF(x: Int): Suspend ?=> Int = x + 1

// case #10
def twoArgumentsZeroContinuationsCF(x: Int, y: Int): Suspend ?=> Int = x + y + 1

//1 continuation
// case #11
def zeroArgumentsSingleResumeContinuations()(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

// case #12
def oneArgumentsSingleResumeContinuations(x: Int)(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + 1)) }

// case #13
def twoArgumentsSingleResumeContinuations(x: Int, y: Int)(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + 1))
  }

// case #14
def zeroArgumentsMultipleResume()(using s: Suspend): Int =
  s.suspendContinuation[Int] { c =>
    c.resume(Right { println("Resume1"); 1 })
    c.resume(Right { println("Resume2"); 2 })
  }

// case #16
def twoCurriedArgumentsOneContinuations(x: Int)(y: Int)(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + 1))
  }

// case #17
def oneArgumentOneAdditionalGivenArgumentOneContinuations(
    x: Int)(using Suspend, String): String =
  summon[Suspend].suspendContinuation[String] { continuation =>
    continuation.resume(Right(summon[String] + x))
  }

// case #18
def genericArgumentsSingleResumeContinuations[A](x: A)(using Suspend): A =
  summon[Suspend].suspendContinuation[A] { continuation => continuation.resume(Right(x)) }

def programOneContinuationReturnValue: Int =
  // case #19
  def zeroArgumentsSingleResumeContinuationsBeforeAfter()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    2
  zeroArgumentsSingleResumeContinuationsBeforeAfter()

  // case #20
  def oneArgumentsSingleResumeContinuationsBeforeAfter(x: Int)(using Suspend): Int =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    println("World")
    2
  oneArgumentsSingleResumeContinuationsBeforeAfter(1)

  // case #21
  def twoArgumentsSingleResumeContinuationsBeforeAfter(x: Int, y: Int)(using Suspend): Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    println("World")
    2
  twoArgumentsSingleResumeContinuationsBeforeAfter(1, 2)

// case #23
def useValsDefinedInsideContinuation()(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    val x = 1
    val y = 2
    continuation.resume(Right(x + y))
  }

// case #24
//def useValsDefinedInsideResume()(using Suspend): Int =
//  summon[Suspend].suspendContinuation[Int] { continuation =>
//    continuation.resume {
//      val x = 1
//      val y = 2
//      Right(x + y)
//    }
//  }

// case #25
def zeroArgumentsSingleResumeContinuationsBefore()(using Suspend): Int =
  println("Hello")
  val x = 1
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

// case #26
def oneArgumentsSingleResumeContinuationsBefore(x: Int)(using Suspend): Int =
  println("Hello")
  val y = x
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

// case #27
def twoArgumentsSingleResumeContinuationsBefore(x: Int, y: Int)(using Suspend): Int =
  println("Hello")
  val z = x + y
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

// case #28
def twoArgumentsSingleResumeContinuationsBeforeUsedInResume(x: Int, y: Int)(
    using Suspend): Int =
  println("Hello")
  val z = 1
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + z))
  }

// case #31
def zeroArgumentsOneContinuationsCF(): Suspend ?=> Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

// case #32
def oneArgumentsOneContinuationsCF(x: Int): Suspend ?=> Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + 1)) }

// case #33
def twoArgumentsOneContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + 1))
  }

//group 5
def programTwoContinuations: Int =
  given String = "Output: "

  // case #34
  def zeroArgumentsTwoContinuations()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  zeroArgumentsTwoContinuations()

  // case #35
  def oneArgumentsTwoContinuations(x: Int)(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

  oneArgumentsTwoContinuations(1)

  // case #36
  def twoArgumentsTwoContinuations(x: Int, y: Int)(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
  twoArgumentsTwoContinuations(1, 2)

  // case #37
  def zeroArgumentsTwoContinuationsMultipleResume()(using s: Suspend): Int =
    s.suspendContinuation[Int] { c => c.resume(Right { println("Resume1"); 1 }) }
    s.suspendContinuation[Int] { c =>
      c.resume(Right {
        println("Resume2"); 1
      })
      c.resume(Right {
        println("Resume3"); 2
      })
    }
  println(Try(zeroArgumentsTwoContinuationsMultipleResume()))

  // case #39
  def twoCurriedArgumentsTwoContinuations(x: Int)(y: Int)(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(3)) }
  twoCurriedArgumentsTwoContinuations(1)(2)

  // case #40
  def oneArgumentOneAdditionalGivenArgumentTwoContinuations(
      x: Int)(using Suspend, String): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(summon[String] + x)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  oneArgumentOneAdditionalGivenArgumentTwoContinuations(1)

  // case #41
  def genericArgumentsContinuations[A](x: A)(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }

  genericArgumentsContinuations(1)

  // case #42
  def zeroArgumentsCodeAfter()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(2)))
    }
    3
  zeroArgumentsCodeAfter()

  // case #43
  def zeroArgumentsCodeBetween()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  zeroArgumentsCodeBetween()

  // case #45
  def zeroArgumentsValsDefinedInsideContinuation()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      val x = 1
      continuation.resume(Right(println(x)))
    }
    val x = 1
    summon[Suspend].suspendContinuation[Int] { continuation =>
      val x = 2
      continuation.resume(Right(x))
    }
  zeroArgumentsValsDefinedInsideContinuation()

  // case #46
//  def zeroArgumentsValsDefinedInsideResume()(using Suspend): Int =
//    summon[Suspend].suspendContinuation[Unit] { continuation =>
//      continuation.resume {
//        val x = 1
//        Right(println(x))
//      }
//    }
//    summon[Suspend].suspendContinuation[Int] { continuation =>
//      continuation.resume {
//        val x = 2
//        Right(x)
//      }
//    }
//  zeroArgumentsValsDefinedInsideResume()

  // case #47
  def zeroArgumentsCodeBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  zeroArgumentsCodeBefore()

  // case #48
  def zeroArgumentsValsDefinedAboveContinuation()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    val y = 2
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(y)) }
  zeroArgumentsValsDefinedAboveContinuation()

  // case #52
  def zeroArgumentsTwoContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(1)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  zeroArgumentsTwoContinuationsCF()

  // case #53
  def oneArgumentsTwoContinuationsCF(x: Int): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  oneArgumentsTwoContinuationsCF(1)

  // case #54
  def twoArgumentsTwoContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(3)) }
  twoArgumentsTwoContinuationsCF(1, 2)

// extras
def programMultipleSuspendWithExpressionsInBody: Int =
  def foo()(using s: Suspend): Int =
    println("Start")
    s.suspendContinuation[Unit] { _.resume(Right { println("Hello") }) }
    println("World")
    val x = 1
    s.suspendContinuation[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume(Right { println(q); false })
    }
    10

  foo()

def left()(using s: Suspend): Int =
  s.suspendContinuation[Int] { continuation =>
    continuation.resume(Left(new Exception("error")))
  }

def programMultipleSuspendLeft =
  def foo()(using s: Suspend): Int =
    s.suspendContinuation[Int] { _.resume(Left(new Exception("error"))) }
    s.suspendContinuation[Int] { _.resume(Right { println("Resume2"); 2 }) }
  foo()

def demoPrints =
  case class Foo(i: Int)
  given String = "Output: "

  println(zeroArgumentsZeroContinuations())
  println(oneArgumentsZeroContinuations(1))
  println(twoArgumentsZeroContinuations(1, 2))
  println(twoCurriedArgumentsZeroContinuations(1)(2))
  println(oneArgumentOneAdditionalGivenArgumentZeroContinuations(1))
  println(genericArgumentsZeroContinuations(Foo(1)))
  println(zeroArgumentsZeroContinuationsCF())
  println(oneArgumentsZeroContinuationsCF(1))
  println(twoArgumentsZeroContinuationsCF(1, 2))

  println(zeroArgumentsSingleResumeContinuations())
  println(oneArgumentsSingleResumeContinuations(1))
  println(twoArgumentsSingleResumeContinuations(1, 2))
  println(Try(zeroArgumentsMultipleResume()))
  println(twoCurriedArgumentsOneContinuations(1)(2))
  println(oneArgumentOneAdditionalGivenArgumentOneContinuations(1))
  println(genericArgumentsSingleResumeContinuations(Foo(1)))

  programOneContinuationReturnValue

  println(useValsDefinedInsideContinuation())
//  println(useValsDefinedInsideResume())
  println(zeroArgumentsSingleResumeContinuationsBefore())
  println(oneArgumentsSingleResumeContinuationsBefore(1))
  println(twoArgumentsSingleResumeContinuationsBefore(1, 2))
  println(twoArgumentsSingleResumeContinuationsBeforeUsedInResume(1, 2))
  println(zeroArgumentsOneContinuationsCF())
  println(oneArgumentsOneContinuationsCF(1))
  println(twoArgumentsOneContinuationsCF(1, 1))

  programTwoContinuations

  println(programMultipleSuspendWithExpressionsInBody)
  println(Try(left()))
  println(Try(programMultipleSuspendLeft))
