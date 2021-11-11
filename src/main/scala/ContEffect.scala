package fx

import cats.effect.IO
import cats.effect.unsafe.IORuntime

trait Effect[R] {
    def shift[A](r: R): A
}


def shift[R : Effect, A](r: R)(using ce: Effect[R]): A =
    ce.shift(r)

def ensure[R](value: Boolean, ensureShift: => R)(using ce: Effect[R]): Unit =
    if (value) () else ce.shift(ensureShift)

extension [R, A](c: Cont[R, A])(using ce: Effect[R])
    def bind: A = c.fold(ce.shift, identity)

extension [R, A](fa: Either[R, A])(using ce: Effect[R])
    def bind: A = fa.fold(ce.shift, identity)

extension [R, A](fa: List[Either[R, A]])(using ce: Effect[R])
    def bind: List[A] = fa.map(either => either.bind)

extension [R, A](fa: IO[Either[R, A]])(using ce: Effect[R], ir: IORuntime)
    def bind: A = fa.unsafeRunSync().bind