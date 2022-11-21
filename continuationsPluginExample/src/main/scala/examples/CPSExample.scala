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

inline def suspendContinuationOrReturn[A](f: Continuation[A] => Continuation.State) = ???

def await[A](future: Future[A]): A =
  suspendContinuationOrReturn { (c: Continuation[A]) =>
    future.onComplete {
      case Success(value) => c.resume(Right(value))
      case Failure(exception) => c.resume(Left(exception))
    }
    Continuation.State.Suspended
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

def programSuspendContinuationTask2: Int =
  // need to cast on the correct expected type?
  def fooTest()(using s: Suspend): Int =
    Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

  fooTest()

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
    Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

  fooTest()
