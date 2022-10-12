package fx

import java.util.concurrent.CancellationException
import scala.util.control.NonFatal
import fx.Raise

type Effect[R, A] = Raise[R] /* { suspend } */ ?=> A

object Fold:
  /**
   * `invoke` the [Effect] and [fold] the result:
   *   - _success_ [transform] result of [A] to a value of [B].
   *   - _raised_ [recover] from `raised` value of [R] to a value of [B].
   *   - _exception_ [error] from [Throwable] by transforming value into [B].
   *
   * This method should never be wrapped in `try`/`catch` as it will not throw any unexpected
   * errors, it will only result in [CancellationException], or fatal exceptions such as
   * `OutOfMemoryError`.
   */
  inline def fold[R, A, B](
      program: Effect[R, A])(recover: (raised: R) => B, transform: (value: A) => B): B =
    fold(program, throw _, recover, transform)

  inline def fold[R, A, B](
      program: Effect[R, A],
      error: (error: Throwable) => B,
      recover: (raised: R) => B,
      transform: (value: A) => B
  ): B =
    val raise = DefaultRaise[R]()
    try transform(program(using raise))
    catch
      case e: CancellationException =>
        recover(raisedOrRethrow(e, raise))
      case NonFatal(e) =>
        error(e)

  /**
   * Returns the shifted value, rethrows the CancellationException if not our scope
   */
  private def raisedOrRethrow[R](exc: CancellationException, raise: DefaultRaise[R]): R =
    exc match
      case e: RaiseCancellationException[_] if raise.eq(e.raise) => e._raised.asInstanceOf[R]
      case _ => throw exc

  /**
   * Serves as both purposes of a scope-reference token, and a default implementation for Raise.
   */
  class DefaultRaise[R] extends Raise[R]:
    extension (r: R) def raise[A]: A = throw RaiseCancellationException(r, this)

  private class RaiseCancellationException[R](val _raised: Any | Null, val raise: Raise[R])
      extends CancellationException("Raised Continuation")
