package fx

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Missing capability:\n* Errors"
)
opaque type Errors = Unit

extension [R](r: R)
  def raise[A]: A * Errors * Control[R] = r.shift
  def ensure(value: Boolean): Unit * Errors * Control[R] =
    if (value) () else r.shift

object Errors:
  given Errors = ()
