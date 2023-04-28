package sttp
package unwrapped

import munit.unwrapped.UnwrappedSuite
import hedgehog.munit.HedgehogAssertions
import _root_.unwrapped.{given, *}
import sttp.unwrapped.StatusCodeToStatusCode.given_StatusCodeToStatusCode_Int_StatusCode
import sttp.unwrapped.StatusCodeToStatusCode.smStatusCode
import sttp.unwrapped.StatusCodeToStatusCode.given_StatusCodeToStatusCode_StatusCode_StatusCode

import hedgehog.Gen

class StatusCodeToStatusCodeSuite extends UnwrappedSuite, HedgehogAssertions:
  lazy val statusCodeInts =
    StatusCode.statusCodes.toList.filterNot(c => c == 509 || c == 425 || c == 418)
  lazy val statusCodeGen = Gen.element(statusCodeInts.head, statusCodeInts.tail)

  propertyFX(
    "Valid status code integers.toStatusCode.toStatusCode.value should be the original integer") {
    for {
      x <- statusCodeGen.forAll
    } yield assertEquals(x.toStatusCode.toStatusCode.toStatusCode.httpValue.code, x)
  }
