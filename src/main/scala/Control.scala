package fx

import scala.annotation.implicitNotFound

/** [[Control]] describes the ability to short-circuit an abilities function
  * with a value of [[R]]
  */
@implicitNotFound(
  "this function may shift control to ${R} and requires capability:\n* Control[${R}]"
)
trait Control[-R]:
  private[fx] val token: String
  /** Short-circuits the computation of [[A]] with a value of [[R]]
    */
  extension (r: R) def shift[A]: A

object Control:
  /** All functions that follow the happy path and consider no control
    * automatically obtain de ability of no Control.
    *
    * Evidence of no control helps monadic values that frequently short-circuit
    * like Either, Option, etc to disregard the Control capability if users just
    * compute through happy paths.
    */
  given Control[Nothing] = new Control[Nothing]:

    private[fx] val token: String = "Control.nothing.token"

    /** ain't got nothing on me
      */
    extension (r: Nothing)
      def shift[A]: A = throw new RuntimeException("impossible")
