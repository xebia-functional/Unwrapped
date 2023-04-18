package examples

import continuations.Continuation.State
import continuations.*
import continuations.intrinsics.*
import continuations.jvm.internal.{ContinuationImpl, SuspendApp}

import scala.annotation.switch
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import concurrent.ExecutionContext.Implicits.global

def await[A](future: Future[A]): A =
  Suspend.given_Suspend.shift { (c: Continuation[A]) =>
    future.onComplete {
      case Success(value) => c.resume(value)
      case Failure(exception) => c.raise(exception)
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
        this.$continuation.raise(exception)
      case Success(value) =>
        this.$continuation.resume(value)

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
        val leftValue = $continuation.result.asInstanceOf[Left[_, _]].value
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
        val leftValue = $continuation.result.asInstanceOf[Left[_, _]].value
        if (leftValue.isInstanceOf[Throwable]) throw leftValue.asInstanceOf[Throwable]
      var10000 = $result
    case 2 =>
      if ($result.isInstanceOf[Left[_, _]])
        val leftValue = $continuation.result.asInstanceOf[Left[_, _]].value
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

def programSuspendContinuationNoParamNoSuspendContinuation =
  def fooTest()(using s: Suspend): Int = 1
  SuspendApp(fooTest())

def programSuspendContinuationNoParamResume =
  def fooTest()(using s: Suspend): Int =
    s.shift[Int] { continuation =>
      println("Hello")
      continuation.resume(1)
    }

  SuspendApp(fooTest())

/*
def programNestedContinuationCompilationError: Int =
  def fooTest()(using s: Suspend): Int =
    s.shift[Int] { continuation =>
      val x = s.shift[Int] { continuation1 => continuation1.resume(1) }
      continuation.resume(x + 1)
    }

  fooTest()
 */

def programSuspendContinuationNoParamResumeIgnoreResult =
  def fooTest()(using s: Suspend): Int =
    println("Start")
    s.shift[Unit] { _.resume(println("Hello")) }
    println("World")
    val x = 1
    s.shift[Boolean] { continuation =>
      val q = "World"
      println("Hi")
      continuation.resume({ println(q); false })
    }
//    s.shift[Int] { continuation =>
//      continuation.raise(new Exception("error"))
//    }
    10

  SuspendApp(fooTest())

def programSuspendContinuationParamDependent =
  def fooTest(qq: Int)(using s: Suspend): Int =
    val pp = 11
    val xx = s.shift[Int] { _.resume(qq - 1) }
    val ww = 13
    val rr = "AAA"
    val yy = s.shift[String] { c => c.resume(rr) }
    val tt = 100
    val zz = s.shift[Int] { _.resume(ww - 1 + xx) }
    println(xx)
    xx + qq + zz + pp + tt + yy.length

  SuspendApp(fooTest(12))

def programSuspendContinuationResumeVals =
  def fooTest()(using Suspend): Int =
    summon[Suspend].shift[Int] { c =>
      c.resume {
        println("Hello")
        val x = 1
        val y = 1
        x + y
      }
    }

  SuspendApp(fooTest())

def programOneContinuationReturnValue =
  def zeroArgumentsSingleResumeContinuationsBeforeAfter()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x))
    }
    println("World")
    2
  SuspendApp(zeroArgumentsSingleResumeContinuationsBeforeAfter())

/*
def programSuspendContinuationNoSuspendContinuationVal: Int =
  val fooTest: Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int =
    [A] =>
      (x: List[A]) =>
        [B] =>
          (y: List[B]) =>
            (q: List[A]) =>
              (p: List[A]) =>
                val z = 1
                x.size + y.size + q.size + p.size + z

  fooTest(List(1))(List("A", "B"))(List(1, 1, 1))(List(1, 1, 1, 1))
*/

// runs only with scalac
object ExampleObject:
  private def method1(x: Int) = x + 1
  protected val z1 = 1

  def continuations(x: Int, y: Int)(using s: Suspend): Int =
    def method2(x: Int) = x + 1
    val z2 = 1

    val suspension1 = s.shift[Int] { continuation =>
      def method3(x: Int) = x
      val z3 = 1

      continuation.resume {
        val z4 = 1
        def method4(x: Int) = x

        method1(x) + 1 + z1 + z2 + method2(y) + z3 + method3(x) + z4 + method4(x)
      }
    }

    s.shift[Int] { continuation => continuation.resume(method1(x) + 1) }

    val z5 = suspension1
    def method5(x: Int) = x

    val suspension2 = s.shift[Int] { continuation =>
      continuation.resume(z5 + suspension1 + method5(y))
    }

    val z6 = 1
    def method6(x: Int) = x

    1 + suspension1 + suspension2 + z6 + method1(x) + method2(x) + method5(x) + method6(x)

end ExampleObject

def programStartContinuation =
  def fooTest()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x))
    }
    println("World")
    2

  SuspendApp(fooTest())