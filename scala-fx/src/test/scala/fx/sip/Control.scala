package fx.sip

import scala.annotation.implicitNotFound

/**
 * [[Control]] describes the ability to short-circuit an abilities function with a value of
 * [[R]]
 */
@implicitNotFound(
  "this function may shift control to ${R} and requires:\n Control[${R}]"
)
trait Control[-R]:
  val token: String

  /**
   * Short-circuits the computation of [[A]] with a value of [[R]]
   */
  extension (r: R) def shift[A]: A


/**
 * Terminal operators for programs that require control
 */
extension [R, A](c: Control[R] ?=> A)

  def toEither: Either[R, A] =
    Continuation.fold(c)(Left(_), Right(_))

  def toOption: Option[A] =
    Continuation.fold(c)(_ => None, Some(_))

  def run: (R | A) = Continuation.fold(c)(identity, identity)