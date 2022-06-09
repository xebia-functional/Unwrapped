package fx

import scala.annotation.implicitNotFound

@implicitNotFound(
  "Missing capability:\n% Errors[${R}]"
)
type Errors[R] = Control[R]

extension [R](r: R)
  def raise[A](using Errors[R]): A = r.shift
  def ensure(value: Boolean)(using Errors[R]): Unit =
    if (value) () else r.shift
