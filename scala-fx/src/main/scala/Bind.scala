package fx

import scala.annotation.implicitNotFound
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.Scheduler
import cats.effect.unsafe.IORuntimeConfig

/**
 * Brings the capability to perform Monad bind in place to any type. Types may access
 * [[Control]] to short-circuit as necessary
 *
 * ```scala
 * import fx.Bind
 * import fx.Control
 *
 * extension [R, A](fa: Either[R, A])
 *   def bind: A % Bind % Control[R] = fa.fold(_.shift, identity)
 * ```
 */
@implicitNotFound(
  "Monadic bind requires capability:\n% Bind"
)
opaque type Bind = Unit

object Bind:
  given Bind = ()

extension [A](fa: List[A]) def bind: A % Bind = ???

extension [R, A](fa: Either[R, A]) def bind: A % Bind % Errors[R] = fa.fold(_.shift, identity)

extension [R, A](fa: List[Either[R, A]]) def bind: List[A] % Bind % Errors[R] = fa.map(_.bind)

extension [A](fa: Option[A])
  def bind: A % Bind % Errors[None.type] = fa.fold(None.shift)(identity)

extension [A](fa: Some[A]) def bind: A % Bind = fa.get
