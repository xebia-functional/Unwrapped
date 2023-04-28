package munit
package unwrapped

import _root_.unwrapped.*
import junit.framework.ComparisonFailure
import org.junit.AssumptionViolatedException
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop._

class ScalaFxAssertionsSuite extends ScalaCheckSuite, UnwrappedAssertions:

  property(
    "given a true condition, assertUnwrapped should return an Errors[AssertionError] ?=> Unit that evaluates to `()` run") {
    forAll { (cond: Boolean) =>
      val obtained: AssertionError | Unit = run(assertUnwrapped(cond))
      cond ==> {
        obtained match
          case x: AssertionError =>
            fail("assertUnwrapped did not evaluate to true")
          case _ => passed
      }
    }
  }

  property(
    "given an exception, assertsShiftsToUnwrapped should expect control to shift to that exception") {
    given Arbitrary[java.lang.Exception] = Arbitrary {
      Gen.alphaNumStr.map(java.lang.Exception(_))
    }
    forAll { (exception: java.lang.Exception) =>
      val obtained = run(assertsShiftsToUnwrapped(exception.shift[Int], exception))
      obtained match
        case obtainedException: FailException =>
          unitToProp(fail(s"Expected unit, got $obtainedException"))
        case _ => passed
    }
  }

  property(
    "given a false condition, assertUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to a `FailException` when run.") {
    forAll { (cond: Boolean) =>
      !cond ==> {
        val obtained = run(assertUnwrapped(false))
        obtained match
          case obtainedException: FailException =>
            passed
          case _ => unitToProp(fail("Expected a FailException, got ()"))
      }
    }
  }

  property(
    "given two equal items, assertEqualUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenInt: Int) =>
      val obtained = run(assertEqualsUnwrapped(givenInt, givenInt))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two inequal items, assertEqualUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (x: Int, y: Int) =>
      (x != y) ==> {
        val obtained =
          run(assertEqualsUnwrapped(x, y))
        obtained match
          case x: org.junit.ComparisonFailure => passed
          case _ => fail("assertion failed to detect inequality")
      }
    }
  }

  property(
    "given a false condition, assumeUnwrapped should return an `Errors[AssumptionViolatedException]` ?=> Unit that evaluates to an AssumptionViolatedException when run") {
    forAll { (cond: Boolean) =>
      (!cond) ==> {
        val obtained = run(assumeUnwrapped(cond))
        obtained match
          case x: AssumptionViolatedException => passed
          case _ => fail("assume failed to detect false assumption")
      }
    }
  }

  property(
    "given a true condition, assumeUnwrapped should return an `Errors[AssumptionViolatedException] ?=> Unit` that evaluates to () when run") {
    forAll { (cond: Boolean) =>
      cond ==> {
        val obtained = run(assumeUnwrapped(cond))
        obtained match
          case x: AssumptionViolatedException => fail("assume failed to detect true assumption")
          case _ => passed
      }
    }
  }

  property(
    "when given two equal strings, assertNoDiffUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to () when run") {
    forAll { (s: String) =>
      val obtained = run(assertNoDiffUnwrapped(s, s))
      obtained match
        case x: ComparisonFailException =>
          fail("assumeNoDiffUnwrapped detected a difference between equal strings")
        case _ => passed
    }
  }

  property(
    "when given two nonequal strings, assertNoDiffUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailureException when run") {
    // assertNoDiff can't handle complex strings in munit
    given Arbitrary[String] = Arbitrary { Gen.alphaLowerStr }
    forAll { (s: String, s2: String) =>
      (s.nonEmpty && s2.nonEmpty && s != s2) ==> {
        val obtained = run(assertNoDiffUnwrapped(s, s2))
        obtained match
          case x: ComparisonFailException =>
            passed
          case _ =>
            fail("assertNoDiffUnwrapped failed to detect a difference between nonequal strings")
      }
    }
  }

  property(
    "given two equal items, assertNotEqualUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (givenInt: Int) =>
      val obtained = run(assertNotEqualsUnwrapped(12, 12))
      obtained match
        case x: ComparisonFailException => passed
        case _ => fail("assertion raised exception when it shouldn't have")
    }
  }

  property(
    "given two inequal items, assertEqualUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (x: Int, y: Int) =>
      (x != y) ==> {
        val obtained =
          run(assertNotEqualsUnwrapped(x, y))
        obtained match
          case x: ComparisonFailException => fail("assertion failed to detect inequality")
          case _ => passed
      }
    }
  }

  property(
    "given two equal items, assertEqualsDoubleUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenDouble: Double) =>
      val obtained = run(assertEqualsDoubleUnwrapped(givenDouble, givenDouble, 0.00))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two items within a tolerance delta range of each other, assertEqualsDoubleUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenDouble: Double, givenDelta: Double) =>
      val givenDoubleWithinDelta = givenDouble + givenDelta - Double.MinPositiveValue
      val obtained =
        run(assertEqualsDoubleUnwrapped(givenDouble, givenDoubleWithinDelta, givenDelta))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two inequal items, assertEqualsDoubleUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (x: Double, y: Double) =>
      (x != y) ==> {
        val obtained =
          run(assertEqualsDoubleUnwrapped(x, y, 0.00))
        obtained match
          case x: org.junit.ComparisonFailure => passed
          case _ => fail("assertion failed to detect inequality")
      }
    }
  }

  property(
    "given two equal items, assertEqualsFloatUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenFloat: Float) =>
      val obtained = run(assertEqualsFloatUnwrapped(givenFloat, givenFloat, 0.00))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two items within a tolerance delta range of each other, assertEqualsFloatUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenFloat: Float, givenDelta: Float) =>
      val givenFloatWithinDelta = givenFloat + givenDelta
      val obtained =
        run(assertEqualsFloatUnwrapped(givenFloat, givenFloatWithinDelta, givenDelta))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two inequal items, assertEqualsFloatUnwrapped should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (x: Float, y: Float) =>
      (x != y) ==> {
        val obtained =
          run(assertEqualsFloatUnwrapped(x, y, 0.00))
        obtained match
          case x: org.junit.ComparisonFailure => passed
          case _ => fail("assertion failed to detect inequality")
      }
    }
  }
