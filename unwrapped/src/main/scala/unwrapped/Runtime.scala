package unwrapped

import scala.annotation.implicitNotFound

extension [R, A](c: Control[R] ?=> A)

  def toEither: Either[R, A] =
    Continuation.fold(c)(Left(_), Right(_))

  def toOption: Option[A] =
    Continuation.fold(c)(_ => None, Some(_))

  def run: (R | A) = Continuation.fold(c)(identity, identity)
