package continuations

import continuations.Continuation.State
import continuations.jvm.internal.BaseContinuationImpl
import continuations.jvm.internal.ContinuationImpl

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService

abstract class Boundary[A]:
  var result: Either[Throwable, Any]

abstract class Starter[A]:
  def invoke(completion: Continuation[A]): A | Any | Null

trait ContinuationLib:
  extension [A](continuation: Continuation[A])
    def intercepted(ec: ExecutionContext): Continuation[A] =
      continuation match
        case x: ContinuationImpl =>
          x.intercepted(ec).asInstanceOf[Continuation[A]]
        case _ => continuation

trait StarterLib extends ContinuationLib:
  extension [A](starter: Starter[A])
    def start(completion: Continuation[A]): Unit =
      create(completion).intercepted(completion.executionContext).resume(())
    def create(completion: Continuation[A]): Continuation[Unit] =
      if (starter.isInstanceOf[ContinuationImpl])
        starter.create(completion)
      else
        starter.createFromCompletion(completion)
    def createFromCompletion(completion: Continuation[A]): Continuation[Unit] =
      val context = completion.context
      if (context == EmptyTuple)
        new RestrictedContinuation(completion.asInstanceOf):
          private var label = 0
          override protected def invokeSuspend(
              result: Either[Throwable, Any | Null | Continuation.State.Suspended.type]): Any |
            Null =
            label match
              case 0 =>
                label = 1
                result match
                  case Left(exception) =>
                    throw exception
                  case _ => ()
                starter.invoke(this)

              case 1 =>
                label = 2
                result match
                  case Left(exception) =>
                    throw exception
                  case Right(result) =>
                    result
              case _ => throw new IllegalStateException("already completed")
      else
        new ContinuationImpl(completion.asInstanceOf, context):
          private var label = 0
          override def invokeSuspend(
              result: Either[Throwable, Any | Null | Continuation.State.Suspended.type])
              : Any | Null | Continuation.State.Suspended.type =
            label match
              case 0 =>
                label = 1
                result match
                  case Left(exception) =>
                    throw exception
                  case _ => ()
                starter.invoke(this)

              case 1 =>
                label = 2
                result match
                  case Left(exception) =>
                    throw exception
                  case Right(result) =>
                    result
              case _ => throw new IllegalStateException("already completed")

object Blocking extends StarterLib:
  val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())
  sys.addShutdownHook(this.close)
  def close: Unit = ec.close()
  def apply[A](block: Continuation[A] ?=> A): A =
    val boundary = new Boundary[A] { var result = null }
    val latch = new CountDownLatch(1)
    val baseContinuation = BuildContinuation(
      ec,
      res => {
        boundary.result = res
        latch.countDown()
      })
    new Starter[A] {
      override def invoke(completion: Continuation[A]): A | Any | Null =
        given Continuation[A] = completion
        block
    }.start(baseContinuation)
    latch.await()
    boundary.result match
      case Left(e) => throw e
      case Right(Right(v)) => v.asInstanceOf[A]
      case Right(v) => v.asInstanceOf[A]

abstract class Deferred[A]:
  private val latch = CountDownLatch(1)
  private var boundary: Boundary[A] = new Boundary {
    override var result: Either[Throwable, Any] = null
  }
  private var completionHandlers: Array[Either[Throwable, Any] => Unit] =
    Array.empty[Either[Throwable, Any] => Unit]
  def resume(res: Either[Throwable, Any]): Unit =
    boundary.result = res
    completionHandlers.foreach(_(res))
    latch.countDown()
  def onComplete(f: Either[Throwable, Any] => Unit): Unit =
    completionHandlers = completionHandlers.appended(f)
  def await(): A =
    latch.await()
    boundary.result match
      case Left(e) => throw e
      case Right(Right(v)) => v.asInstanceOf[A]
      case Right(v) => v.asInstanceOf[A]

object Defer extends StarterLib:
  val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())
  sys.addShutdownHook(this.close)
  def close: Unit = ec.close()
  def apply[A](block: Continuation[A] => A): Deferred[A] =
    val boundary = new Deferred[A] {}
    val baseContinuation = BuildContinuation(
      ec,
      boundary.resume
    )
    new Starter[A] {
      override def invoke(completion: Continuation[A]): A | Any | Null = block(completion)
    }.start(baseContinuation)
    boundary

extension [A](deferreds: scala.collection.IterableOnce[Deferred[A]])
  def awaitAll: List[A] =
    val latch = CountDownLatch(deferreds.size - 1)
    var results = List[A]()
    deferreds.foreach { d =>
      d.onComplete { r =>
        latch.countDown()
        results = results.appended(
          r match
            case Left(e) => throw e
            case Right(Right(v)) => v.asInstanceOf[A]
            case Right(v) => v.asInstanceOf[A]
        )
      }
    }
    latch.await()
    results
