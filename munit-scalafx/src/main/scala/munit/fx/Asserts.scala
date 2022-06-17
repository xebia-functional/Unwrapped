package munit
package fx

import scala.annotation.implicitNotFound

/**
 * The Asserts copability Models assertion error-handling capabilities.
 *
 * AssertionError falls outside the hierarchy covered by the Throws capability,
 * @tparam R
 *   Contravariant assertion error subtype
 */
@implicitNotFound(
  "Missing capability:\n" +
    "* Asserts[${R}]\n" +
    "alternatively you may resolve this call with \n" +
    "```scala\nhandle(f)(recover)\n```\n" +
    "or\n ignore thrown exceptions with import munit.fx.unsafe.unsafeAssertions`")
opaque type Asserts[-R <: AssertionError] = Unit

object Asserts {

  /**
   * Importable capability evidence that allows unsafely throwing assertion errors when
   * imported.
   * @tparam R
   *   The assertion error subtype that is allowod to be thrown.
   */
  given unsafeAsserts[R <: AssertionError]: Asserts[R] = ()
}

/**
 * Inline-optimization for handling AssertionErrors safely.
 *
 * @tparam R
 *   The assertion error subtype to handle
 * @tparam A
 *   The expected type of successful evaluation
 * @recover
 *   A total function that can recover from a state generating an R.
 * @return
 *   The result of running f or recover
 */
inline def handleAssert[R <: AssertionError, A](
    inline f: Asserts[R] ?=> A
)(inline recover: (R) => A): A =
  try
    import munit.fx.Asserts.unsafeAsserts
    f
  catch
    case r: R =>
      recover(r)
