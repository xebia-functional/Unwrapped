package fx

import cats.effect.IO
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Exiting Control requires capability:\n* Runtime"
)
opaque type Runtime = Unit

given runtime: Runtime = ()

extension [R, A](c: A * Control[R])

  def run: (R | A) * Runtime = fold(c)(identity, identity)

  def toEither: Either[R, A] * Runtime =
    fold(c)(Left(_), Right(_))

  def toIO: IO[Either[R, A]] * Runtime = {
    given Control[R] = summon[Control[R]]
    IO(toEither)
  }