package munit
package fx

import _root_.fx.*
import junit.framework.ComparisonFailure
import org.junit.AssumptionViolatedException
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop._

class ScalaFxAssertionsSuite extends ScalaCheckSuite, ScalaFxAssertions:

  property(
    "given a true condition, assertFX should return an Errors[AssertionError] ?=> Unit that evaluates to `()` run") {
    forAll { (cond: Boolean) =>
      val obtained: AssertionError | Unit = run(assertFX(cond))
      cond ==> {
        obtained match
          case x: AssertionError =>
            fail("assertFX did not evaluate to true")
          case _ => passed
      }
    }
  }
    
  property("given an exception, assertsShiftsToFX should expect control to shift to that exception") {
    given Arbitrary[java.lang.Exception] = Arbitrary {
      Gen.alphaNumStr.map(java.lang.Exception(_))
    }
    forAll { (exception: java.lang.Exception) =>
      val obtained = run(assertsShiftsToFX(exception.shift[Int], exception))
      obtained match
        case obtainedException: FailException =>
          unitToProp(fail(s"Expected unit, got $obtainedException"))
        case _ => passed
    }
  }

  property(
    "given a false condition, assertFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to a `FailException` when run.") {
    forAll { (cond: Boolean) =>
      !cond ==> {
        val obtained = run(assertFX(false))
        obtained match
          case obtainedException: FailException =>
            passed
          case _ => unitToProp(fail("Expected a FailException, got ()"))
      }
    }
  }

  property(
    "given two equal items, assertEqualFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenInt: Int) =>
      val obtained = run(assertEqualsFX(givenInt, givenInt))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two inequal items, assertEqualFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (x: Int, y: Int) =>
      (x != y) ==> {
        val obtained =
          run(assertEqualsFX(x, y))
        obtained match
          case x: org.junit.ComparisonFailure => passed
          case _ => fail("assertion failed to detect inequality")
      }
    }
  }

  property(
    "given a false condition, assumeFX should return an `Errors[AssumptionViolatedException]` ?=> Unit that evaluates to an AssumptionViolatedException when run") {
    forAll { (cond: Boolean) =>
      (!cond) ==> {
        val obtained = run(assumeFX(cond))
        obtained match
          case x: AssumptionViolatedException => passed
          case _ => fail("assume failed to detect false assumption")
      }
    }
  }

  property(
    "given a true condition, assumeFX should return an `Errors[AssumptionViolatedException] ?=> Unit` that evaluates to () when run") {
    forAll { (cond: Boolean) =>
      cond ==> {
        val obtained = run(assumeFX(cond))
        obtained match
          case x: AssumptionViolatedException => fail("assume failed to detect true assumption")
          case _ => passed
      }
    }
  }

  property(
    "when given two equal strings, assertNoDiffFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to () when run") {
    forAll { (s: String) =>
      val obtained = run(assertNoDiffFX(s, s))
      obtained match
        case x: ComparisonFailException =>
          fail("assumeNoDiffFX detected a difference between equal strings")
        case _ => passed
    }
  }

  property(
    "when given two nonequal strings, assertNoDiffFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailureException when run") {
    // assertNoDiff can't handle complex strings in munit
    given Arbitrary[String] = Arbitrary { Gen.alphaLowerStr }
    forAll { (s: String, s2: String) =>
      (s.nonEmpty && s2.nonEmpty && s != s2) ==> {
        val obtained = run(assertNoDiffFX(s, s2))
        obtained match
          case x: ComparisonFailException =>
            passed
          case _ =>
            fail("assertNoDiffFX failed to detect a difference between nonequal strings")
      }
    }
  }

  property(
    "given two equal items, assertNotEqualFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (givenInt: Int) =>
      val obtained = run(assertNotEqualsFX(12, 12))
      obtained match
        case x: ComparisonFailException => passed
        case _ => fail("assertion raised exception when it shouldn't have")
    }
  }

  property(
    "given two inequal items, assertEqualFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (x: Int, y: Int) =>
      (x != y) ==> {
        val obtained =
          run(assertNotEqualsFX(x, y))
        obtained match
          case x: ComparisonFailException => fail("assertion failed to detect inequality")
          case _ => passed
      }
    }
  }

  property(
    "given two equal items, assertEqualsDoubleFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenDouble: Double) =>
      val obtained = run(assertEqualsDoubleFX(givenDouble, givenDouble, 0.00))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two items within a tolerance delta range of each other, assertEqualsDoubleFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenDouble: Double, givenDelta: Double) =>
      val givenDoubleWithinDelta = givenDouble + givenDelta - Double.MinPositiveValue
      val obtained = run(assertEqualsDoubleFX(givenDouble, givenDoubleWithinDelta, givenDelta))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two inequal items, assertEqualsDoubleFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (x: Double, y: Double) =>
      (x != y) ==> {
        val obtained =
          run(assertEqualsDoubleFX(x, y, 0.00))
        obtained match
          case x: org.junit.ComparisonFailure => passed
          case _ => fail("assertion failed to detect inequality")
      }
    }
  }

  property(
    "given two equal items, assertEqualsFloatFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenFloat: Float) =>
      val obtained = run(assertEqualsFloatFX(givenFloat, givenFloat, 0.00))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two items within a tolerance delta range of each other, assertEqualsFloatFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to `()` when run") {
    forAll { (givenFloat: Float, givenDelta: Float) =>
      val givenFloatWithinDelta = givenFloat + givenDelta
      val obtained = run(assertEqualsFloatFX(givenFloat, givenFloatWithinDelta, givenDelta))
      obtained match
        case x: ComparisonFailure =>
          fail("assertion raised exception when it shouldn't have")
        case _ => passed
    }
  }

  property(
    "given two inequal items, assertEqualsFloatFX should return an `Errors[AssertionError] ?=> Unit` that evaluates to a ComparisonFailure when run") {
    forAll { (x: Float, y: Float) =>
      (x != y) ==> {
        val obtained =
          run(assertEqualsFloatFX(x, y, 0.00))
        obtained match
          case x: org.junit.ComparisonFailure => passed
          case _ => fail("assertion failed to detect inequality")
      }
    }
  }
