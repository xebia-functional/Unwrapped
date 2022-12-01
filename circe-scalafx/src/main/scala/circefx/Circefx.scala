package circefx

import cats._
import cats.data._
import cats.implicits._
import fx.{given, _}
import io.circe._
import io.circe.parser._
import io.circe.syntax._

object Circefx:

  def parsing[E, A](s: String)(
      using cx: Control[NonEmptyList[E] | ParsingFailure | DecodingFailure],
      d: Decoder[A]): A =
    parse(s).bind.as[A].bind

  def encoding[E, A](instance: A)(using cx: Control[NonEmptyList[E]], e: Encoder[A]) =
    instance.asJson
