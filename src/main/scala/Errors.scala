package fx

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Missing capability:\n* Errors[${R}]"
)
type Errors[R] = Control[R]

extension [R](r: R)
  def raise[A]: A * Errors[R] = r.shift
  def ensure(value: Boolean): Unit * Errors[R] =
    if (value) () else r.shift

