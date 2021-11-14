package fx

import cats.effect.IO

trait Cont[R, A] {
  def fold[B](f: R => B, g: A => B): B
}

inline def fold[R, A, B](inline fra: Effect[R] ?=> A, f: R => B, g: A => B) : B =
  cont(fra).fold(f, g)

extension [R, A](c: Cont[R, A])

  def toEither: Either[R, A] =
    c.fold(Left(_), Right(_))

  def toIO: IO[Either[R, A]] =
    IO(toEither)

  def map[B](f: A => B): Cont[R, B] =
    cont(c.fold(shift(_), f))

extension [A](c: Cont[Option[A], A])
   
  def toOption: Option[A] =
    c.fold(identity, Some(_))


inline def cont[R, A](inline f: Effect[R] ?=> A): Cont[R, A] =
  new Coroutine(f)

