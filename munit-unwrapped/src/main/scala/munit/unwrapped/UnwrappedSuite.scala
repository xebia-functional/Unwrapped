package munit
package unwrapped

import _root_.unwrapped._
import org.junit.AssumptionViolatedException
import hedgehog.core.Seed
import hedgehog.{runner => hr}

import scala.annotation.targetName
import scala.reflect.Typeable
import hedgehog.core.PropertyConfig
import hedgehog.Property
import hedgehog.munit.HedgehogAssertions
import hedgehog.core.Status

/**
 * Provides functionality for testing within a scala-unwrapped context.
 */
abstract class UnwrappedSuite extends FunSuite, UnwrappedAssertions, HedgehogAssertions:

  private val seedSource = hr.SeedSource.fromEnvOrTime()
  private val seed = Seed.fromLong(seedSource.seed)

  private def check(test: hr.Test, config: PropertyConfig)(implicit loc: Location): Any = {
    val report = Property.check(test.withConfig(config), test.result, seed)
    if (report.status != Status.ok) {
      val reason = hr
        .Test
        .renderReport(
          this.getClass.getName,
          test,
          report,
          ansiCodesSupported = true
        )
      withMunitAssertions(assertions => assertions.fail(s"$reason\n${seedSource.renderLog}"))
    }
  }

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
    def testUnwrapped[R, F <: AssertionError: Typeable](name: String)(
        body: Errors[R | F] ?=> A => Unit): Location ?=> Unit =
      a.test(TestOptions(name)) { fixture =>
        val x: R | F | Unit = run(structured(body(fixture)))
        x match
          case _: Unit => ()
          case ex: F => throw ex
          case x => throw AssertionError(x)
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
    def testUnwrapped[R, F <: AssertionError: Typeable](options: TestOptions)(
        body: Errors[R | F] ?=> A => Unit): Location ?=> Unit =
      a.test(options) { fixture =>
        val x: R | F | Unit = run(structured(body(fixture)))
        x match
          case _: Unit => ()
          case ex: F => throw ex
          case x => throw AssertionError(x)
      }

    /**
     * Provides testFX for [munit.FunFixtures.FunFixture]
     *
     * @tparam R
     *   The error type declared by the test body effect.
     * @tparam F
     *   AssertionError thrown by any assertions
     * @param name
     *   The name of the test
     * @param withConfig
     *   A hedgehog PropertyConfig transformer
     * @return
     *   The result of the test when a location can be pulled from given scope.
     */
    def propertyWithFixtureFX[R, F <: AssertionError: Typeable](
        name: String,
        withConfig: PropertyConfig => PropertyConfig = identity)(
        prop: Errors[R | F] ?=> A => Property): Location ?=> Unit =
      a.testUnwrapped(name) { fixture =>
        val t = hedgehog.runner.property(name, prop(fixture)).config(withConfig)
        check(t, t.withConfig(PropertyConfig.default))
      }
  }

  /**
   * Provides testFX for [munit.FunFixtures.FunFixture]
   *
   * @tparam R
   *   The error type declared by the test body effect.
   * @tparam F
   *   AssertionError thrown by any assertions
   * @param name
   *   The name of the test
   * @param withConfig
   *   A hedgehog PropertyConfig transformer
   * @return
   *   The result of the test when a location can be pulled from given scope.
   */
  def propertyFX[R, F <: AssertionError: Typeable](
      name: String,
      withConfig: PropertyConfig => PropertyConfig = identity)(
      prop: Errors[R | F] ?=> Property): Location ?=> Unit =
    testUnwrapped(name) {
      val t = hedgehog.runner.property(name, prop).config(withConfig)
      check(t, t.withConfig(PropertyConfig.default))
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
  def testUnwrapped[R, F <: AssertionError: Typeable](name: String)(
      body: Errors[R | F] ?=> Unit): Location ?=> Unit =
    test(name) {
      val x: R | F | Unit = run(structured(body))
      x match {
        case _: Unit => ()
        case ex: F => throw ex
        case x => throw AssertionError(x)
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
  def testUnwrapped[R, F <: AssertionError: Typeable](options: TestOptions)(
      body: => Errors[R | F] ?=> Unit): Location ?=> Unit =
    test(options) {
      val x: R | F | Unit = run(structured(body))
      x match {
        case _: Unit => ()
        case ex: F => throw ex
        case x => throw AssertionError(x)
      }
    }
