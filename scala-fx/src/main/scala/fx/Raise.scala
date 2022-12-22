package fx

import scala.annotation.implicitNotFound

/**
 * [[Raise]] describes the ability to short-circuit function with a value of [[R]]
 */
@implicitNotFound(
  "this function may raise a ${R} and requires capability:\n% Raise[${R}]"
)
trait Raise[-R]:
  /**
   * Short-circuits the computation of [[A]] with a value of [[R]]
   */
  extension (r: R) def raise: Nothing

extension [R, A](fa: Either[R, A]) def bind(using Raise[R]): A = fa.fold(_.raise, identity)
extension [R, A](fa: Right[R, A]) def bind: A = fa.value
extension [A](fa: Option[A]) def bind(using Raise[None.type]): A = fa.fold(None.raise)(identity)
extension [A](fa: Some[A]) def bind: A = fa.value
