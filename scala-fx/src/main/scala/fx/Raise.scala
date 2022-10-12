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
  extension (r: R) def raise[A]: A
