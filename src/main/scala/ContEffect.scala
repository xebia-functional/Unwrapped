package fx

import cats.effect.IO
import cats.effect.unsafe.IORuntime

trait Control[R] :
  extension (r: R) 
    def shift[A]: A
  
  def ensure[R](value: Boolean, ensureShift: => R): Unit |> Control[R] =
    if (value) () else ensureShift.shift


class Bind:
  extension [R, A](c: A |> Control[R])
    def bind: A |> Control[R] = fold(c)(_.shift, identity)

  extension [R, A](fa: Either[R, A])
    def bind: A |> Control[R] = fa.fold(_.shift, identity)

  extension [R, A](fa: List[Either[R, A]])
    def bind: List[A] |> Control[R] = fa.map(_.bind)

  extension [R, A](fa: IO[Either[R, A]])
    def bind: A |> Control[R] |> IORuntime = fa.unsafeRunSync().bind

object Bind :
  given Bind = new Bind