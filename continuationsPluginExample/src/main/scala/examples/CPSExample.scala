package examples

import continuations.Continuation.State
import continuations.*
import continuations.intrinsics.*
import continuations.jvm.internal.ContinuationImpl

import scala.annotation.switch
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import concurrent.ExecutionContext.Implicits.global

def await[A](future: Future[A]): A =
  Suspend.given_Suspend.suspendContinuation { (c: Continuation[A]) =>
    future.onComplete {
      case Success(value) => c.resume(Right(value))
      case Failure(exception) => c.resume(Left(exception))
    }
  }

def await$expanded[A](future: Future[A])(using completion: Continuation[Any | Null]): Any =
  val f: Try[A] => Unit = it => {
    val $await = new await$2$1(completion)
    $await.invoke(it)
  }
  future.onComplete(f)
  val var10000: Any | Null =
    if (future.isCompleted) Continuation.State.Resumed else Continuation.State.Suspended
  var10000

final class await$2$1(completion: Continuation[Any | Null])
    extends ContinuationImpl(completion, completion.context):
  // $FF: synthetic field
  val $continuation = completion

  final def invokeSuspend(result: Either[Throwable, Any]): Any = ???
  final def invoke[A](it: Try[A]): Unit =
    it match
      case Failure(exception) =>
        this.$continuation.resume(Left(exception))
      case Success(value) =>
        this.$continuation.resume(Right(value))

def a(): Int = 47
def b(): Unit = ()
def c(z: Int): Int = z

def foo(x: Int): Future[Int] = Future(x)
def bar(x: Int, y: Int): Future[Int] = Future(x + y)

def program: Suspend ?=> Int =
  val x: Int = a()
  val y: Int = await(foo(x)) // suspension point #1
  b()
  val z = await(bar(x, y)) // suspension point #2
  c(z)

import continuations.jvm.internal.ContinuationImpl

final class program$continuation$1(override val completion: Continuation[Any | Null])
    extends ContinuationImpl(completion, completion.context) {
  var I$0 = 0
  // $FF: synthetic field
  var result: Any = null
  var label = 0

  final def invokeSuspend(result: Either[Throwable, Any | Null | State.Suspended.type]): Any |
    Null =
    this.result = result
    this.label |= Integer.MIN_VALUE
    program$expanded(using this)
}

def program$expanded(
    using var0: Continuation[Any | Null]): Any | Null | Continuation.State.Suspended.type =
  val $continuation: program$continuation$1 =
    if (var0.isInstanceOf[program$continuation$1])
      if ((var0.asInstanceOf[program$continuation$1].label & Integer.MIN_VALUE) != 0)
        var0.asInstanceOf[program$continuation$1].label -= Integer.MIN_VALUE
      var0.asInstanceOf[program$continuation$1]
    else new program$continuation$1(var0)
  var var10000: Any | Null = null
  val $result = $continuation.result
  val var6 = Continuation.State.Suspended
  var x: Int = null.asInstanceOf[Int]
  var var7: Future[Any] = null.asInstanceOf[Future[Any]]
  var executeAfterMatch = true
  ($continuation.label) match
    case 0 =>
      if ($result.isInstanceOf[Left[_, _]])
        val leftValue = $result.asInstanceOf[Left[_, _]].value
        if (leftValue.isInstanceOf[Throwable]) throw leftValue.asInstanceOf[Throwable]
      x = a()
      var7 = foo(x)
      $continuation.I$0 = x
      $continuation.label = 1
      var10000 = await$expanded(var7)(using $continuation)
      if (var10000 == var6) return var6
    case 1 =>
      x = $continuation.I$0
      if ($result.isInstanceOf[Left[_, _]])
        val leftValue = $result.asInstanceOf[Left[_, _]].value
        if (leftValue.isInstanceOf[Throwable]) throw leftValue.asInstanceOf[Throwable]
      var10000 = $result
    case 2 =>
      if ($result.isInstanceOf[Left[_, _]])
        val leftValue = $result.asInstanceOf[Left[_, _]].value
        if (leftValue.isInstanceOf[Throwable]) throw leftValue.asInstanceOf[Throwable]
        executeAfterMatch = false
      var10000 = $result
    case _ =>
      throw new IllegalStateException("call to `resume` before continuation is ready")
  if (executeAfterMatch)
    val y: Int = var10000.asInstanceOf[Number].intValue
    b()
    var7 = bar(x, y)
    $continuation.label = 2
    var10000 = await$expanded(var7)(using $continuation)
    if (var10000 == var6) return var6
  val z: Int = var10000.asInstanceOf[Number].intValue
  c(z)

def programSuspendContinuationNoParamNoSuspendContinuation: Int =
  def fooTest()(using s: Suspend): Int = 1
  fooTest()

def programSuspendContinuationNoParamResume: Int =
  def fooTest()(using s: Suspend): Int =
    s.suspendContinuation[Int] { continuation =>
      println("Hello")
      continuation.resume(Right(1))
    }

  fooTest()

/*
def programNestedContinuationCompilationError: Int =
  def fooTest()(using s: Suspend): Int =
    s.suspendContinuation[Int] { continuation =>
      val x = s.suspendContinuation[Int] { continuation1 => continuation1.resume(Right(1)) }
      continuation.resume(Right(x + 1))
    }

  fooTest()
 */

def programSuspendContinuationNoParamResumeIgnoreResult: Int =
  def fooTest()(using s: Suspend): Int =
    println("Start")
    s.suspendContinuation[Unit] { _.resume(Right { println("Hello") }) }
    println("World")
    val x = 1
    s.suspendContinuation[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume(Right { println(q); false })
    }
//    s.suspendContinuation[Int] { continuation =>
//      continuation.resume(Left(new Exception("error")))
//    }
    10

  fooTest()

def programSuspendContinuationParamDependent: Int =
  def fooTest(qq: Int)(using s: Suspend): Int =
    val pp = 11
    val xx = s.suspendContinuation[Int] { _.resume(Right { qq - 1 }) }
    val ww = 13
    val rr = "AAA"
    val yy = s.suspendContinuation[String] { c => c.resume(Right { rr }) }
    val tt = 100
    val zz = s.suspendContinuation[Int] { _.resume(Right { ww - 1 + xx }) }
    println(xx)
    xx + qq + zz + pp + tt + yy.length

  fooTest(12)

def programSuspendContinuationResumeVals: Int =
  def fooTest()(using Suspend): Int =
    summon[Suspend].suspendContinuation[Int] { c =>
      c.resume {
        println("Hello")
        val x = 1
        val y = 1
        Right(x + y)
      }
    }

  fooTest()

def programTest: Int =
  // defs
  def fooD1: Suspend ?=> Int = 1
  def fooD2: Suspend ?=> (String, Int) => Int = (w, x) => x + w.length
  def fooD3: Suspend ?=> (Int, String) => Int => Int = (x, w) => y => y + x + w.length
  def fooD4: Suspend ?=> (Int, String) => Int ?=> Int = (x, w) => x + w.length + summon[Int]
  def fooD5: Int => Suspend ?=> Int = x => x
  def fooD6: Int => Suspend ?=> Int => Int = x => y => x + y
  def fooD7: Int ?=> Suspend ?=> Int = summon[Int]
  def fooD8: (Int, Suspend) ?=> Int => Int = x => x + summon[Int]
  def fooD9: Int => Int => Suspend ?=> Int => String => Int => Int = x =>
    y => z => w => q => x + y + z + w.length + q
  def fooD10: Int => Int => Suspend ?=> (String, Boolean) => Int => Int = x =>
    z => (w, b) => y => x + y + z + w.length + b.toString.length
  def fooD11: (Suspend, Int) ?=> Int => Int => Int = x => y => x + summon[Int] + y
  def fooD12: Int => (String, Boolean) => Int => Suspend ?=> Int => Int = x =>
    (w, b) => z => y => x + y + z + w.length + b.toString.length
  def fooD13(x: Int, y: Int)(z: Int, k: Int): Suspend ?=> Int ?=> (Int, Int) => Int =
    (m, n) => x + y + z + k + m + n + summon[Int]
  def fooD14: List[Int] => Suspend ?=> Int = l => l.size
  def fooD15[A]: List[A] => Suspend ?=> Int = l => l.size
  def fooD16: List[String] ?=> Suspend ?=> Int = summon[List[String]].size
  def fooD17: Int ?=> Suspend ?=> String ?=> List[String] ?=> Int =
    summon[Int] + summon[String].length + summon[List[String]].size
  def fooD18: Suspend ?=> Int ?=> String ?=> List[String] ?=> Int =
    summon[Int] + summon[String].length + summon[List[String]].size
  def fooD19(using Suspend, Int): String ?=> List[String] ?=> Int =
    summon[Int] + summon[String].length + summon[List[String]].size
  def fooD20: (Int, Suspend) ?=> String ?=> Int => Int =
    x => x + summon[Int] + summon[String].length
  def fooD21: Suspend ?=> Int =
    println("Hello")
    val x = 3
    1 + x
  def fooD22: Suspend ?=> Int => Int ?=> Int = y =>
    val x = 3
    println("Hello")
    1 + x + y + summon[Int]

  // vals
  val fooV1: Suspend ?=> Int = 1
  val fooV2: Suspend ?=> (String, Int) => Int = (w, x) => x + w.length
  val fooV3: Suspend ?=> (Int, String) => Int => Int = (x, w) => y => y + x + w.length
  val fooV4: Suspend ?=> (Int, String) => Int ?=> Int = (x, w) => x + w.length + summon[Int]
  val fooV5: Int => Suspend ?=> Int = x => x
  val fooV6: Int => Suspend ?=> Int => Int = x => y => x + y
  val fooV7: Int ?=> Suspend ?=> Int = summon[Int]
  val fooV8: (Int, Suspend) ?=> Int => Int = x => x + summon[Int]
  val fooV9: Int => Int => Suspend ?=> Int => String => Int => Int = x =>
    y => z => w => q => x + y + z + w.length + q
  val fooV10: Int => Int => Suspend ?=> (String, Boolean) => Int => Int = x =>
    z => (w, b) => y => x + y + z + w.length + b.toString.length
  val fooV11: (Suspend, Int) ?=> Int => Int => Int = x => y => x + summon[Int] + y
  val fooV12: Int => (String, Boolean) => Int => Suspend ?=> Int => Int = x =>
    (w, b) => z => y => x + y + z + w.length + b.toString.length
  val fooV14: List[Int] => Suspend ?=> Int = l => l.size
  val fooV15: Suspend ?=> [A] => List[A] => Int = [A] => (list: List[A]) => list.size
  val fooV16: List[String] ?=> Suspend ?=> Int = summon[List[String]].size
  val fooV17: Int ?=> Suspend ?=> String ?=> List[String] ?=> Int =
    summon[Int] + summon[String].length + summon[List[String]].size
  val fooV18: Suspend ?=> Int ?=> String ?=> List[String] ?=> Int =
    summon[Int] + summon[String].length + summon[List[String]].size
  val fooV20: (Int, Suspend) ?=> String ?=> Int => Int =
    x => x + summon[Int] + summon[String].length
  val fooV21: Suspend ?=> Int =
    println("Hello")
    val x = 3
    1 + x
  val fooV22: Suspend ?=> Int => Int ?=> Int = y =>
    val x = 3
    println("Hello")
    1 + x + y + summon[Int]

  given Int = 3
  given String = "AA"
  given List[String] = List("AA", "Q")

  println("CF START")
  println(fooD1)
  println(fooD2("AA", 1))
  println(fooD3(1, "AA")(3))
  println(fooD4(1, "AA"))
  println(fooD5(1))
  println(fooD6(1)(2))
  println(fooD7)
  println(fooD8(1))
  println(fooD9(1)(2)(3)("AAAA")(5))
  println(fooD10(1)(2)("AAA", false)(4))
  println(fooD11(1)(2))
  println(fooD12(1)("AA", true)(3)(4))
  println(fooD13(1, 2)(3, 4)(5, 6))
  println(fooD14(List(1, 2)))
  println(fooD15(List("AA", "AA")))
  println(fooD16)
  println(fooD17)
  println(fooD18)
  println(fooD19)
  println(fooD20(1))
  println(fooD21)
  println(fooD22(1))
  println("CF END")

  println("CF VALS START")
  println(fooV1)
  println(fooV2("AA", 1))
  println(fooV3(1, "AA")(3))
  println(fooV4(1, "AA"))
  println(fooV5(1))
  println(fooV6(1)(2))
  println(fooV7)
  println(fooV8(1))
  println(fooV9(1)(2)(3)("AAAA")(5))
  println(fooV10(1)(2)("AAA", false)(4))
  println(fooV11(1)(2))
  println(fooV12(1)("AA", true)(3)(4))
  println(fooV14(List(1, 2)))
  println(fooV15(List("AA", "AA")))
  println(fooV16)
  println(fooV17)
  println(fooV18)
  println(fooV20(1))
  println(fooV21)
  println(fooV22(1))
  println("CF VALS END")
  2
