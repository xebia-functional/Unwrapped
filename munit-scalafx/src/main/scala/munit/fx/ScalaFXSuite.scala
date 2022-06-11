package munit
package fx

import _root_.fx._
import org.junit.AssumptionViolatedException

import scala.annotation.targetName

/**
 * Provides functionality for testing within a scala-fx context.
 */
abstract class ScalaFXSuite extends FunSuite, ScalaFxAssertions:

  /**
   * Provides fixtures to effectful tests.
   * @tparam A
   * @param a
   *   The fixture to provide to the tests
   */
  extension [A](a: FunFixture[A]) {

    /**
     * Provides testFX for [munit.FunFixtures.FunFixture]
     *
     * @tparam R
     *   The error type declared by the test body effect.
     * @tparam B
     * @param name
     *   The unique name of the test within the suite.
     * @param body
     *   A test body that requires an error effect from given scope and the fixture.
     * @return
     *   The result of the test when a location can be pulled from given scope.
     */
    def testFX[R <: Throwable, B](name: String)(body: Errors[R] ?=> A => B): Location ?=> Unit =
      a.test(TestOptions(name)) { fixture =>
        val x: R | B = run(structured(body(fixture)))
        x match
          case ex: Throwable => throw ex
          case x => ()
      }

    /**
     * Provides testFX for [munit.FunFixtures.FunFixture]
     *
     * @tparam R
     *   The error type declared by the test body effect.
     * @tparam B
     * @param options
     *   The TestOptions containing then names, tags, and other options munit provides.
     * @param body
     *   A test body that requires an error effect from given scope and the fixture.
     * @return
     *   The result of the test when a location can be pulled from given scope.
     */
    def testFX[R <: Throwable, B](options: TestOptions)(
        body: Errors[R] ?=> A => B): Location ?=> Unit =
      a.test(options) { fixture =>
        val x: R | B = run(structured(body(fixture)))
        x match
          case ex: Throwable => throw ex
          case x => ()
      }
  }

  /**
   * Runs a test inside an effect. The execution of the effect is delayed until the test is run.
   *
   * @tparam R
   *   The error type returned by running the test body effect. Required to be a throwable due
   *   to munit test evaluations.
   * @tparam A
   *   The return type of the test body
   * @param name
   *   A unique string identifying the test within the suite.
   * @param body
   *   A test program suspended in an effect that at minumum can shift control to the R error
   *   type.
   * @return
   *   Unit
   */
  def testFX[R <: Throwable, A](name: String)(body: Errors[R] ?=> A): Location ?=> Unit =
    test(name) {
      val x: R | A = run(structured(body))
      x match {
        case ex: Throwable => throw ex
        case x => ()
      }
    }

  /**
   * Runs a test inside an effect. The execution of the effect is delayed until the test is run.
   *
   * @tparam R
   *   The error type returned by running the test body effect. Required to be a throwable due
   *   to munit test evaluations.
   * @tparam A
   *   The return type of the test body
   * @param options
   *   The options, including the name, tag, and other munit test configuration options
   * @param body
   *   A test program suspended in an effect that at minumum can shift control to the R error
   *   type.
   * @return
   *   Unit
   */
  def testFX[R <: Throwable, A](options: TestOptions)(
      body: => Errors[R] ?=> A): Location ?=> Unit =
    test(options) {
      val x: R | A = run(structured(body))
      x match {
        case ex: Throwable => throw ex
        case x => ()
      }
    }
