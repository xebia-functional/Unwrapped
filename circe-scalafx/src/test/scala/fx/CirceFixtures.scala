package circefx

import _root_.fx.{given, *}
import cats.*
import cats.implicits.*
import cats.syntax.*
import munit.fx.ScalaFXSuite
import io.circe.Decoder
import io.circe.HCursor
import io.circe.Decoder.Result

val testJson: String =
  """{
    |  "test":"blah"
    |}
      """.stripMargin

case class TestEncoder(x: Int, y: String)
object TestEncoder:

  given d:Decoder[TestEncoder] = new Decoder[TestEncoder] {

    override def apply(c: HCursor): Result[TestEncoder] = (c.downField("x").as[Int], c.downField("y").as[String]).mapN(TestEncoder(_, _))


  }

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
    setup = _ => TestEncoder(1, "test"),
    teardown = _ => ()
  )

  val testEncoderInstance = FunFixture(setup = _ => TestEncoder(1, "test"), teardown = _ => ())

  val decodingDerived = FunFixture(
    setup = _ => {
      """{"x":1,"y":"test"}"""
    },
    teardown = _ => ()
  )

  val testEncoderInstanceAndDecoderDerived = FunFixture.map2(testEncoderInstance, decodingDerived)
}
