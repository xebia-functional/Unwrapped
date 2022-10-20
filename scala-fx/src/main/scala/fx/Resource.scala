package fx

import java.util.concurrent.atomic.AtomicReference
import scala.util.control.NonFatal
import Tuple.{InverseMap, IsMappedBy, Map}
import scala.annotation.implicitNotFound

class Resource[A](
    val acquire: Resources ?=> () => A,
    val release: (A, ExitCase) => Unit
):

  def this(value: Resources ?=> () => A) = this(value, (_, _) => ())

  def use[B](f: A => B): B = bracketCase(acquire, f, release)

  def map[B](f: (A) => B): Resource[B] =
    Resource(() => f(this.bind))

  def flatMap[B](f: (A) => Resource[B]): Resource[B] =
    Resource(() => f(this.bind).bind)

  def bind: A =
    bracketCase(
      () => {
        val a = acquire()
        val finalizer: (ExitCase) => Unit = (ex: ExitCase) => release(a, ex)
        summon[Resources].finalizers.updateAndGet(finalizer +: _)
        a
      },
      identity,
      (a, ex) =>
        // Only if ExitCase.Failure, or ExitCase.Cancelled during acquire we cancel
        // Otherwise we've saved the finalizer, and it will be called from somewhere else.
        if (ex != ExitCase.Completed)
          val e = cancelAll(summon[Resources].finalizers.get(), ex, null)
          val e2 =
            try
              release(a, ex)
              null
            catch case NonFatal(e) => e
          val error = composeErrors(e, e2)
          throw error
    )

class Resources(val finalizers: AtomicReference[List[(ExitCase) => Unit]])

object Resources:
  given Resources = new Resources(
    AtomicReference(
      List.empty
    ))

enum ExitCase:
  case Completed
  case Cancelled(exception: Throwable)
  case Failure(failure: Throwable)

private[fx] def cancelAll(
    finalizers: List[(ExitCase) => Unit],
    exitCase: ExitCase,
    first: Throwable | Null = null
): Throwable | Null = finalizers
  .map(f => {
    try
      f(exitCase)
      null
    catch case NonFatal(e) => e
  })
  .fold(first)((it, acc) =>
    if (acc != null) composeErrors(acc, it)
    else it)

private[fx] def composeErrors(
    left: Throwable | Null,
    right: Throwable | Null
): Throwable | Null =
  left match
    case l: Throwable =>
      right match
        case r: Throwable =>
          l.addSuppressed(r)
          l
        case null => l
    case null => right
