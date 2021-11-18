package fx

import cats.effect.IO

extension [R, A](c: A |> Control[R])

  def run: R | A = fold(c)(identity, identity)

  def toEither: Either[R, A] =
    fold(c)(Left(_), Right(_))

  def toIO: IO[Either[R, A]] = {
    given Control[R] = summon[Control[R]]
    IO(toEither)
  }