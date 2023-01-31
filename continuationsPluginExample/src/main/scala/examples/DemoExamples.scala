package examples

import continuations.Suspend

import scala.util.Try

//group 1
def zeroArgumentsZeroContinuations()(using Suspend): Int = 1
def oneArgumentsZeroContinuations(x: Int)(using Suspend): Int = x + 1
def twoArgumentsZeroContinuations(x: Int, y: Int)(using Suspend): Int = x + y + 1
def genericArgumentsZeroContinuations[A](a: A)(using Suspend): A = a
def twoCurriedArgumentsZeroContinuations(x: Int)(y: Int)(using Suspend): Int = x + y + 1
def oneArgumentOneAdditionalGivenArgumentZeroContinuations(
    x: Int)(using Suspend, String): String = summon[String] + x
def zeroArgumentsZeroContinuationsCF(): Suspend ?=> Int = 1
def oneArgumentsZeroContinuationsCF(x: Int): Suspend ?=> Int = x + 1
def twoArgumentsZeroContinuationsCF(x: Int, y: Int): Suspend ?=> Int = x + y + 1

//group 2
def zeroArgumentsSingleResumeContinuations()(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
def oneArgumentsSingleResumeContinuations(x: Int)(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + 1)) }
def twoArgumentsSingleResumeContinuations(x: Int, y: Int)(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + 1))
  }
def genericArgumentsSingleResumeContinuations[A](x: A)(using Suspend): A =
  summon[Suspend].suspendContinuation[A] { continuation => continuation.resume(Right(x)) }
def twoCurriedArgumentsOneContinuations(x: Int)(y: Int)(using Suspend): Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + 1))
  }
def oneArgumentOneAdditionalGivenArgumentOneContinuations(
    x: Int)(using Suspend, String): String =
  summon[Suspend].suspendContinuation[String] { continuation =>
    continuation.resume(Right(summon[String] + x))
  }
def zeroArgumentsOneContinuationsCF(): Suspend ?=> Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
def oneArgumentsOneContinuationsCF(x: Int): Suspend ?=> Int =
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + 1)) }
def twoArgumentsOneContinuationsCF(x: Int, y: Int): Suspend ?=> Int =
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + 1))
  }

//group 3
def zeroArgumentsSingleResumeContinuationsBefore()(using Suspend): Int =
  println("Hello")
  val x = 1
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x)) }
def oneArgumentsSingleResumeContinuationsBefore(x: Int)(using Suspend): Int =
  println("Hello")
  val y = 1
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + y)) }
def twoArgumentsSingleResumeContinuationsBefore(x: Int, y: Int)(using Suspend): Int =
  println("Hello")
  val z = 1
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + z))
  }
def genericArgumentsSingleResumeContinuationsBefore[A](x: A)(using Suspend): A =
  println("Hello")
  val y = 1
  summon[Suspend].suspendContinuation[A] { continuation => continuation.resume(Right(x)) }
def twoCurriedArgumentsOneContinuationsBefore(x: Int)(y: Int)(using Suspend): Int =
  println("Hello")
  val z = 1
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + z))
  }
def oneArgumentOneAdditionalGivenArgumentOneContinuationsBefore(
    x: Int)(using Suspend, String): String =
  println("Hello")
  val z = 1
  summon[Suspend].suspendContinuation[String] { continuation =>
    continuation.resume(Right(summon[String] + x + z))
  }
def zeroArgumentsOneContinuationsCFBefore(): Suspend ?=> Int =
  println("Hello")
  val x = 1
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x)) }
def oneArgumentsOneContinuationsCFBefore(x: Int): Suspend ?=> Int =
  println("Hello")
  val y = 1
  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + y)) }
def twoArgumentsOneContinuationsCFBefore(x: Int, y: Int): Suspend ?=> Int =
  println("Hello")
  val z = 1
  summon[Suspend].suspendContinuation[Int] { continuation =>
    continuation.resume(Right(x + y + z))
  }

//group 4
def programOneContinuation: Int =
  case class Bar(x: Int)
  given String = "Output: "

  def zeroArgumentsSingleResumeContinuationsBeforeAfter()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    2
  zeroArgumentsSingleResumeContinuationsBeforeAfter()

  def oneArgumentsSingleResumeContinuationsBeforeAfter(x: Int)(using Suspend): Int =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    println("World")
    2
  oneArgumentsSingleResumeContinuationsBeforeAfter(1)
  def twoArgumentsSingleResumeContinuationsBeforeAfter(x: Int, y: Int)(using Suspend): Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    println("World")
    2
  twoArgumentsSingleResumeContinuationsBeforeAfter(1, 2)
  def genericArgumentsSingleResumeContinuationsBeforeAfter[A](x: A)(using Suspend): A =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    x
  genericArgumentsSingleResumeContinuationsBeforeAfter(Bar(1))
  def twoCurriedArgumentsOneContinuationsBeforeAfter(x: Int)(y: Int)(using Suspend): Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    println("World")
    2
  twoCurriedArgumentsOneContinuationsBeforeAfter(1)(2)
  def oneArgumentOneAdditionalGivenArgumentOneContinuationsBeforeAfter(
      x: Int)(using Suspend, String): String =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(summon[String] + x + z)))
    }
    "World"
  oneArgumentOneAdditionalGivenArgumentOneContinuationsBeforeAfter(1)(2)
  def zeroArgumentsOneContinuationsCFBeforeAfter(): Suspend ?=> Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    2
  zeroArgumentsOneContinuationsCFBeforeAfter()
  def oneArgumentsOneContinuationsCFBeforeAfter(x: Int): Suspend ?=> Int =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    println("World")
    2
  oneArgumentsOneContinuationsCFBeforeAfter(1)
  def twoArgumentsOneContinuationsCFBeforeAfter(x: Int, y: Int): Suspend ?=> Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    println("World")
    2
  twoArgumentsOneContinuationsCFBeforeAfter(1, 2)

//group 5
def programTwoContinuations: Int =
  case class Bar(x: Int)
  given String = "Output: "

  def zeroArgumentsSingleResumeTwoContinuationsBeforeAfter()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(y)))
    }
    2
  zeroArgumentsSingleResumeTwoContinuationsBeforeAfter()

  def oneArgumentsSingleResumeTwoContinuationsBeforeAfter(x: Int)(using Suspend): Int =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    println("World")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + z)))
    }
    2
  oneArgumentsSingleResumeTwoContinuationsBeforeAfter(1)
  def twoArgumentsSingleResumeTwoContinuationsBeforeAfter(x: Int, y: Int)(using Suspend): Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    println("World")
    val q = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    2
  twoArgumentsSingleResumeTwoContinuationsBeforeAfter(1, 2)
  def genericArgumentsSingleResumeTwoContinuationsBeforeAfter[A](x: A)(using Suspend): A =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    val q = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    x
  genericArgumentsSingleResumeTwoContinuationsBeforeAfter(Bar(1))
  def twoCurriedArgumentsTwoContinuationsBeforeAfter(x: Int)(y: Int)(using Suspend): Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    println("World")
    val q = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    2
  twoCurriedArgumentsTwoContinuationsBeforeAfter(1)(2)
  def oneArgumentOneAdditionalGivenArgumentTwoContinuationsBeforeAfter(
      x: Int)(using Suspend, String): String =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(summon[String] + x + z)))
    }
    val q = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    "World"
  oneArgumentOneAdditionalGivenArgumentTwoContinuationsBeforeAfter(1)(2)
  def zeroArgumentsTwoContinuationsCFBeforeAfter(): Suspend ?=> Int =
    println("Hello")
    val x = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x)))
    }
    println("World")
    val q = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    2
  zeroArgumentsTwoContinuationsCFBeforeAfter()
  def oneArgumentsTwoContinuationsCFBeforeAfter(x: Int): Suspend ?=> Int =
    println("Hello")
    val y = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y)))
    }
    println("World")
    val q = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    2
  oneArgumentsTwoContinuationsCFBeforeAfter(1)
  def twoArgumentsTwoContinuationsCFBeforeAfter(x: Int, y: Int): Suspend ?=> Int =
    println("Hello")
    val z = 1
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(x + y + z)))
    }
    val q = 1
    println("World")
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(q)))
    }
    2
  twoArgumentsTwoContinuationsCFBeforeAfter(1, 2)

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

def programMultipleResume()(using s: Suspend): Int =
  s.suspendContinuation[Int] { c =>
    c.resume(Right { println("Resume1"); 1 })
    c.resume(Right { println("Resume2"); 2 })
  }

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
  println(genericArgumentsZeroContinuations(Foo(1)))
  println(twoCurriedArgumentsZeroContinuations(1)(2))
  println(oneArgumentOneAdditionalGivenArgumentZeroContinuations(1))
  println(zeroArgumentsZeroContinuationsCF())
  println(oneArgumentsZeroContinuationsCF(1))
  println(twoArgumentsZeroContinuationsCF(1, 1))

  println(zeroArgumentsSingleResumeContinuations())
  println(oneArgumentsSingleResumeContinuations(1))
  println(twoArgumentsSingleResumeContinuations(1, 2))
  println(genericArgumentsSingleResumeContinuations(Foo(1)))
  println(twoCurriedArgumentsOneContinuations(1)(2))
  println(oneArgumentOneAdditionalGivenArgumentOneContinuations(1))
  println(zeroArgumentsOneContinuationsCF())
  println(oneArgumentsOneContinuationsCF(1))
  println(twoArgumentsOneContinuationsCF(1, 1))

  println(zeroArgumentsSingleResumeContinuationsBefore())
  println(oneArgumentsSingleResumeContinuationsBefore(1))
  println(twoArgumentsSingleResumeContinuationsBefore(1, 2))
  println(genericArgumentsSingleResumeContinuationsBefore(Foo(1)))
  println(twoCurriedArgumentsOneContinuationsBefore(1)(2))
  println(oneArgumentOneAdditionalGivenArgumentOneContinuationsBefore(1))
  println(zeroArgumentsOneContinuationsCFBefore())
  println(oneArgumentsOneContinuationsCFBefore(1))
  println(twoArgumentsOneContinuationsCFBefore(1, 1))

  programOneContinuation
  programTwoContinuations
  println(programMultipleSuspendWithExpressionsInBody)
  println(Try(programMultipleResume()))
  println(Try(left()))
  println(Try(programMultipleSuspendLeft))
