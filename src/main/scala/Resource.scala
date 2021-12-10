package fx

import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference
import scala.util.control.NonFatal
import scala.annotation.varargs
import cats.syntax.validated

class Resource[A](
    val acquire: A * Resources,
    val release: (A, ExitCase) => Unit
):

  def this(value: A * Resources) = this(value, (_, _) => ())

  def use[B](f: A => B): B * Resources = bracketCase(() => acquire, f, release)

  def map[B](f: (A) => B): Resource[B] =
    given Resources = new Resources
    Resource(f(this.bind))

  def flatMap[B](f: (A) => Resource[B]): Resource[B] =
    given Resources = new Resources
    Resource(f(this.bind).bind)

  def bind: A * Resources =
    given resources: Resources = summon[Resources]
    bracketCase(
      () => {
        val a = acquire
        val finalizer: (ExitCase) => Unit = (ex: ExitCase) => release(a, ex)
        resources.finalizers.updateAndGet(finalizer +: _)
        a
      },
      identity,
      (a, ex) =>
        // Only if ExitCase.Failure, or ExitCase.Cancelled during acquire we cancel
        // Otherwise we've saved the finalizer, and it will be called from somewhere else.
        if (ex != ExitCase.Completed)
          val e = cancelAll(resources.finalizers.get(), ex, null)
          val e2 =
            try
              release(a, ex)
              null
            catch case NonFatal(e) => e
          val error = composeErrors(e, e2)
          throw error
    )

class Resources:
  val finalizers: AtomicReference[List[(ExitCase) => Unit]] = AtomicReference(
    List.empty
  )

object Resources:
  given Resources = new Resources

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
    else it
  )

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

def parallelZip[X <: Tuple, C](
    fa: X,
    f: (TupledVarargs[Resource, X]#Result) => C
)(using
    X =:= TupledVarargs[Resource, X]#Args
): Resource[C] * Resources * Structured =
  Resource({
    val results =
      fa.toList
        .map(fa => () => fa.asInstanceOf[Resource[Any]].bind)
        .map(fork(_))
    joinAll
    val r = Tuple
      .fromArray(results.map(_.join).toArray)
      .asInstanceOf[TupledVarargs[Resource, X]#Result]
    f(r)
  })
