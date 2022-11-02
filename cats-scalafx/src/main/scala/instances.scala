package fx
package instances

import _root_.cats.Monad
import _root_.cats.effect.Async
import _root_.cats.effect.Outcome
import _root_.cats.effect.kernel.{Cont, Deferred, Fiber, Poll, Ref, Sync}
import _root_.cats.instances.*
import fx.{ExitCase, Structured, cancel, fork, join, uncancellable as FxUncancellable, Fiber as FxFiber}

import java.util.concurrent.{CancellationException, CompletableFuture, ExecutionException, Executor, Executors, TimeUnit}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

type StructuredF[A] = Structured ?=> A

class FxAsync extends Async[StructuredF]:

  override def raiseError[A](e: Throwable): StructuredF[A] =
    throw e

  override def pure[A](x: A): StructuredF[A] =
    x

  override def sleep(time: FiniteDuration): StructuredF[Unit] =
    Thread.sleep(time.toMillis)

  override def handleErrorWith[A](fa: StructuredF[A])(f: Throwable => StructuredF[A]): StructuredF[A] =
    try fa
    catch case NonFatal(ex) => f(ex)

  override def executionContext: StructuredF[ExecutionContext] =
    ExecutionContext.fromExecutor(
      Executors.newThreadPerTaskExecutor(summon[Structured].threadFactory))

  override def cont[K, R](body: Cont[StructuredF, K, R]): StructuredF[R] =
    Async.defaultCont(body)(this)

  override def suspend[A](hint: Sync.Type)(thunk: => A): StructuredF[A] =
    thunk

  override def ref[A](a: A): StructuredF[Ref[StructuredF, A]] =
    Ref.unsafe(a)(this)

  override def deferred[A]: StructuredF[Deferred[StructuredF, A]] =
    Deferred.unsafe(this)

  override def start[A](fa: StructuredF[A]): StructuredF[Fiber[StructuredF, Throwable, A]] =
    val ourFiber: FxFiber[A] = fork(() => fa)
    new Fiber[StructuredF, Throwable, A]:
      override def join: StructuredF[Outcome[StructuredF, Throwable, A]] =
        try Outcome.succeeded(ourFiber.join)
        catch
            case (_: CancellationException) => Outcome.canceled
            case (_: ExecutionException) => Outcome.canceled
            case NonFatal(t) => Outcome.errored(t)

      override def cancel: StructuredF[Unit] = ourFiber.cancel()

  override def cede: StructuredF[Unit] =
    fork(() => ()).join

  override def forceR[A, B](fa: StructuredF[A])(fb: StructuredF[B]): StructuredF[B] =
    try
      val _ = fa
      fb
    catch case NonFatal(_) => fb

  override def uncancelable[A](body: Poll[StructuredF] => StructuredF[A]): StructuredF[A] =
    FxUncancellable(() => {
      val poll = new Poll[StructuredF] {
        override def apply[A](f: StructuredF[A]): StructuredF[A] =
          fork(() => f).join
      }
      body(poll)
    })

  override def canceled: StructuredF[Unit] =
    throw CancellationException()

  override def onCancel[A](fa: StructuredF[A], fin: StructuredF[Unit]): StructuredF[A] =
      try fa
      catch
        case e: CancellationException =>
          val _ = fin
          throw e

  override def flatMap[A, B](fa: StructuredF[A])(f: A => StructuredF[B]): StructuredF[B] =
    f(fa)

  override def tailRecM[A, B](a: A)(f: A => StructuredF[Either[A, B]]): StructuredF[B] =
    FxAsync.functionMonad.tailRecM(a)(a => () => f(a))()

  override def monotonic: StructuredF[FiniteDuration] =
    FiniteDuration(System.nanoTime(), TimeUnit.NANOSECONDS)

  override def realTime: StructuredF[FiniteDuration] =
    FiniteDuration(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

  override def evalOn[A](fa: StructuredF[A], ec: ExecutionContext): StructuredF[A] =
    val promise = CompletableFuture[A]()
    ec.execute(() => promise.complete(fa))
    promise.join()


object FxAsync:
  val functionMonad = function.catsStdBimonadForFunction0

  given asyncInstance: Async[StructuredF] =
    new FxAsync
