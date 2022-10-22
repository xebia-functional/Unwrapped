package circefx

import munit.fx.ScalaFXSuite
// import java.lang.Throwable

import fx.*
import fx.Control
import fx.Control.*
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import Circefx.*

class CirceSuite extends ScalaFXSuite, CirceFixtures:
  combined.testFX("Circe FX test") {
    case (x: Int, y: String) =>
      assertEqualsFX(x, 1)
  }

  // jsonError.testFX("JSON error") {
  //   case (errorJson: String) =>
  //     val test = parsing[Throwable, Json](errorJson)
  //     assertEqualsFX(1, 1)
  // }

  jsonTest.testFX("JSON test") {
    case (testJson: String) =>
      val test = parsing[Throwable, Json](testJson)
      assertEqualsFX(1, 1)
  }
