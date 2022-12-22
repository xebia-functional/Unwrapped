package munit
package fx

import _root_.fx.*
import org.junit.AssumptionViolatedException

/**
 * Wraps munit assertions in a Raise effect for integration within scala-fx test bodies.
 */
trait ScalaFxAssertions:
  self: Assertions =>

  /**
   * Lifts munit.Assertions#assert into Raise for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param cond
   *   The assertion value
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertFX[R](cond: => Raise[R] ?=> Boolean, clue: => Any = "assertion failed")
      : Location ?=> (Raise[AssertionError], Raise[R]) ?=> Unit =
    liftToFX(assert(cond, clue))

  /**
   * Lifts munit.Assertions#assertEquals into Raise for testing. Will not return until given an
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
  def assertEqualsFX[R, A, B](
      obtained: Raise[R] ?=> A,
      expected: B,
      clue: => Any = "values are not the same"
  ): Location ?=> B <:< A ?=> (Raise[AssertionError], Raise[R]) ?=> Unit =
    liftToFX(assertEquals(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assume into Raise for testing. Will not return until given an
   * Error[AssumptionViolatedException], typically within run or structured.
   *
   * @param cond
   *   The assertion value
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assumeFX[R](cond: => Raise[R] ?=> Boolean, clue: => Any = "assumption failed")
      : Location ?=> (Raise[AssumptionViolatedException], Raise[R]) ?=> Unit =
    try assume(cond, clue)
    catch case ex: AssumptionViolatedException => ex.raise

  /**
   * Lifts munit.Assertions#assertNoDiff into Raise for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertNoDiffFX[R](
      obtained: Raise[R] ?=> String,
      expected: String,
      clue: => Any = "diff assertion failed")
      : Location ?=> (Raise[AssertionError], Raise[R]) ?=> Unit =
    liftToFX(assertNoDiff(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assertNotEquals into Raise for testing. Will not return until given
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
  def assertNotEqualsFX[R, A, B](
      obtained: Raise[R] ?=> A,
      expected: B,
      clue: => Any = "values are the same"
  ): Location ?=> A =:= B ?=> (Raise[AssertionError], Raise[R]) ?=> Unit =
    liftToFX(assertNotEquals(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assertEqualsDoubleFX into Raise for testing. Will not return until
   * given an Error[AssertionError], typically within run or structured.
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
  def assertEqualsDoubleFX[R](
      obtained: Raise[R] ?=> Double,
      expected: Double,
      delta: Double,
      clue: => Any = "values are not the same"
  ): Location ?=> (Raise[AssertionError], Raise[R]) ?=> Unit = liftToFX(
    assertEqualsDouble(obtained, expected, delta, clue))

  /**
   * Lifts munit.Assertions#assertEquals into Raise for testing. Will not return until given an
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
  def assertEqualsFloatFX[R](
      obtained: Raise[R] ?=> Float,
      expected: Float,
      delta: Float,
      clue: => Any = "values are not the same"
  ): Location ?=> (Raise[AssertionError], Raise[R]) ?=> Unit = liftToFX(
    assertEqualsFloat(obtained, expected, delta, clue))

  def assertsRaisesToFX[R, T, A](
      obtained: Raise[R] ?=> A,
      expected: T): (Location, Raise[R | AssertionError]) ?=> Unit =
    obtained
      .toEither
      .fold(
        r => assertEqualsFX(r, expected),
        a => FailException(s"expected $expected got $a", summon[Location]))

  private def liftToFX[A](body: Asserts[AssertionError] ?=> A): Raise[AssertionError] ?=> Unit =
    handleAssert(body)(_.raise)
