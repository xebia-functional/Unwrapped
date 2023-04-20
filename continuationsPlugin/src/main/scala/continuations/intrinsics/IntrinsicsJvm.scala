package continuations.intrinsics

import continuations.jvm.internal.{BaseContinuationImpl, ContinuationImpl, Starter}
import continuations.{Continuation, RestrictedContinuation, Suspend}

import scala.concurrent.ExecutionContext

extension [A](continuation: Continuation[A])
  def intercepted(ec: ExecutionContext): Continuation[A] =
    if (continuation.isInstanceOf[ContinuationImpl])
      continuation.asInstanceOf[ContinuationImpl].intercepted(ec).asInstanceOf[Continuation[A]]
    else continuation

extension [A](suspendedFn: Starter ?=> A)

  // inline def shift
  /*
  suspend inline fun <T> suspendCoroutineUninterceptedOrReturn(
      crossinline block: (Continuation<T>) -> Any?
  ): T
   */

  def startContinuation(completion: Continuation[A]): Unit =
    createContinuation(completion).intercepted(completion.executionContext).resume(())

  inline def startContinuationOrSuspend(
      completion: Continuation[A]): Any | Null | Continuation.State.Suspended.type =
    suspendedFn
      .asInstanceOf[Continuation[A] => (Any | Null | Continuation.State.Suspended.type)](
        completion)

  def createContinuation(completion: Continuation[A]): Continuation[Unit] =
    if (suspendedFn.isInstanceOf[BaseContinuationImpl])
      suspendedFn.asInstanceOf[BaseContinuationImpl].create(completion)
    else
      createContinuationFromSuspendFunction(
        completion,
        (continuation: Continuation[A]) => {
          suspendedFn.asInstanceOf[Starter].invoke(continuation)
        }
      )

private inline def createContinuationFromSuspendFunction[T](
    completion: Continuation[T],
    block: Continuation[T] => T | Any | Null
): Continuation[Unit] =
  val context = completion.context
  if (context == EmptyTuple)
    new RestrictedContinuation(completion.asInstanceOf) {
      private var label = 0

      override protected def invokeSuspend(result: Either[Throwable, Any | Null])
          : Any | Null | Continuation.State.Suspended.type =
        label match
          case 0 =>
            label = 1
            result match
              case Left(exception) =>
                throw exception
              case _ => ()
            block(this)

          case 1 =>
            label = 2
            result match
              case Left(exception) =>
                throw exception
              case Right(result) =>
                result
          case _ => throw new IllegalStateException("already completed")

    }
  else
    new ContinuationImpl(completion.asInstanceOf, context) {
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
            block(this)

          case 1 =>
            label = 2
            result match
              case Left(exception) =>
                throw exception
              case Right(result) =>
                result
          case _ => throw new IllegalStateException("already completed")

    }
