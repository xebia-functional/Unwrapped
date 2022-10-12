package fx

import scala.annotation.implicitNotFound

extension [R, A](c: Raise[R] ?=> A)

  def toEither: Either[R, A] =
    Fold.fold(c)(Left(_), Right(_))

  def toOption: Option[A] =
    Fold.fold(c)(_ => None, Some(_))

  def run: (R | A) = Fold.fold(c)(identity, identity)
