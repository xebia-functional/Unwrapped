package circefx

import munit.fx.ScalaFXSuite

import fx.*
import fx.Control
import fx.Control.*
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import Circefx.*

import cats.implicits._
import cats.syntax._

import io.circe._, io.circe.generic.semiauto._

class CirceSuite extends ScalaFXSuite, CirceFixtures:
  jsonTest.testFX("JSON test") {
    case (testJson: String) =>
      val test: Json = parsing(testJson)
      assertFX(test.toString().size > 0)
  }

  jsonError.testFX("JSON error".fail) {
    case (errorJson: String) =>
      val test: Json = parsing(errorJson)
      assertFX(test.toString().size > 0)
  }

  jsonError.testFX("JSON error") {
    case (errorJson: String) =>
      assertsShiftsToException(
        parsing[Throwable, Json](errorJson),
        ParsingFailure(
          "expected json value got \'error ...\' (line 1, column 1)",
          new Exception()))
  }

  successfullEncoding.testFX("Encoding success") {
    case (instance: List[Int]) =>
      val test: Json = encoding(instance)
      assertFX(test =!= Json.Null)
  }

  encondingDerived.testFX("Encoding derived") {
    case (testEnc: TestEncoder) =>
      given e: Encoder[TestEncoder] = deriveEncoder[TestEncoder]
      val test: Json = encoding(testEnc)
      assertFX(test =!= Json.Null)
  }

  testEncoderInstanceAndDecoderDerived.testFX("Decoding test") {
    (expected: TestEncoder, givenJsonStr: String) =>
      assertEqualsFX(parsing[Throwable, TestEncoder](givenJsonStr), expected)
  }
