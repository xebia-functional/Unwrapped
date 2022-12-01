package circefx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite

val testJson: String =
  """{
    |  "test":"blah"
    |}
      """.stripMargin

case class TestEnconder(x: Int, y: String)

trait CirceFixtures { self: ScalaFXSuite =>
  val jsonError = FunFixture(
    setup = _ => "error Json",
    teardown = _ => ()
  )

  val jsonTest = FunFixture(
    setup = _ => testJson,
    teardown = _ => ()
  )

  val successfullEncoding = FunFixture(
    setup = _ => List(1, 2, 3),
    teardown = _ => ()
  )

  val encondingDerived = FunFixture(
    setup = _ => TestEnconder(1, "test"),
    teardown = _ => ()
  )
}
