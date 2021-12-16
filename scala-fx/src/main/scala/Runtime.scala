package fx

import scala.annotation.implicitNotFound

@implicitNotFound(
  "Exiting Control requires capability:\n% Runtime"
)
opaque type Runtime = Unit

given runtime: Runtime = ()

extension [R, A](c: A % Control[R])

  def toEither: Either[R, A] % Runtime =
    Continuation.fold(c)(Left(_), Right(_))

  def toOption: Option[A] % Runtime =
    Continuation.fold(c)(_ => None, Some(_))

  def run: (R | A) % Runtime = Continuation.fold(c)(identity, identity)
