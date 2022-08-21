package sttp
package fx

import munit.fx.ScalaFXSuite
import hedgehog.munit.HedgehogAssertions
import _root_.fx.{given, *}
import sttp.fx.StatusCodeToStatusCode.given_StatusCodeToStatusCode_Int_StatusCode
import sttp.fx.StatusCodeToStatusCode.smStatusCode
import sttp.fx.StatusCodeToStatusCode.given_StatusCodeToStatusCode_StatusCode_StatusCode

import hedgehog.Gen

class StatusCodeToStatusCodeSuite extends ScalaFXSuite, HedgehogAssertions:
  lazy val statusCodeInts = StatusCode.statusCodes.toList.filterNot(c => c == 509 || c == 425)
  lazy val statusCodeGen = Gen.element(statusCodeInts.head, statusCodeInts.tail)

  propertyFX(
    "Valid status code integers.toStatusCode.toStatusCode.value should be the original integer") {
    for {
      x <- statusCodeGen.forAll
    } yield assertEquals(x.toStatusCode.toStatusCode.toStatusCode.httpValue.code, x)
  }
