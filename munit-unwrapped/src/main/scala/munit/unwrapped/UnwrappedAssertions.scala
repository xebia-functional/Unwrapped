package munit
package unwrapped

import _root_.unwrapped.*
import org.junit.AssumptionViolatedException

/**
 * Wraps munit assertions in an Errors effect for integration within scala-unwrapped test
 * bodies.
 */
trait UnwrappedAssertions:
  self: Assertions =>

  /**
   * Lifts munit.Assertions#assert into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param cond
   *   The assertion value
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertUnwrapped[R](cond: => Errors[R] ?=> Boolean, clue: => Any = "assertion failed")
      : Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
    liftToUnwrapped(assert(cond, clue))

  /**
   * Lifts munit.Assertions#assertEquals into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @tparam A
   *   the type of the obtained value
   * @tparam B
   *   the type of the expected value
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertEqualsUnwrapped[R, A, B](
      obtained: Errors[R] ?=> A,
      expected: B,
      clue: => Any = "values are not the same"
  ): Location ?=> B <:< A ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
    liftToUnwrapped(assertEquals(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assume into Errors for testing. Will not return until given an
   * Error[AssumptionViolatedException], typically within run or structured.
   *
   * @param cond
   *   The assertion value
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assumeUnwrapped[R](cond: => Errors[R] ?=> Boolean, clue: => Any = "assumption failed")
      : Location ?=> (Errors[AssumptionViolatedException], Errors[R]) ?=> Unit =
    try assume(cond, clue)
    catch case ex: AssumptionViolatedException => ex.raise

  /**
   * Lifts munit.Assertions#assertNoDiff into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertNoDiffUnwrapped[R](
      obtained: Errors[R] ?=> String,
      expected: String,
      clue: => Any = "diff assertion failed")
      : Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
    liftToUnwrapped(assertNoDiff(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assertNotEquals into Errors for testing. Will not return until given
   * an Error[AssertionError], typically within run or structured.
   *
   * @tparam A
   *   the type of the obtained value
   * @tparam B
   *   the type of the expected value
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertNotEqualsUnwrapped[R, A, B](
      obtained: Errors[R] ?=> A,
      expected: B,
      clue: => Any = "values are the same"
  ): Location ?=> A =:= B ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
    liftToUnwrapped(assertNotEquals(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assertEqualsDoubleUnwrapped into Errors for testing. Will not return
   * until given an Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param delta
   *   Acceptable error tolerance between the expected and obtained values
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertEqualsDoubleUnwrapped[R](
      obtained: Errors[R] ?=> Double,
      expected: Double,
      delta: Double,
      clue: => Any = "values are not the same"
  ): Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit = liftToUnwrapped(
    assertEqualsDouble(obtained, expected, delta, clue))

  /**
   * Lifts munit.Assertions#assertEquals into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param delta
   *   Acceptable error tolerance between the expected and obtained values
   * @param clue
   * @see
   *   munit.Clue
   */
  def assertEqualsFloatUnwrapped[R](
      obtained: Errors[R] ?=> Float,
      expected: Float,
      delta: Float,
      clue: => Any = "values are not the same"
  ): Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit = liftToUnwrapped(
    assertEqualsFloat(obtained, expected, delta, clue))

  def assertsShiftsToUnwrapped[R, T, A](
      obtained: Errors[R] ?=> A,
      expected: T): (Location, Errors[R | AssertionError]) ?=> Unit =
    obtained
      .toEither
      .fold(
        r => assertEqualsUnwrapped(r, expected),
        a => FailException(s"expected $expected got $a", summon[Location]))

  private def liftToUnwrapped[A](
      body: munit.unwrapped.Asserts[AssertionError] ?=> A): Errors[AssertionError] ?=> Unit =
    munit.unwrapped.handleAssert(body)(_.raise)
