package fx

import scala.annotation.implicitNotFound

extension [R, A](c: Raise[R] ?=> A)

  def toEither: Either[R, A] =
    fold(c)(Left(_), Right(_))

  def toOption: Option[A] =
    fold(c)(_ => None, Some(_))

  def run: (R | A) = fold(c)(identity, identity)
