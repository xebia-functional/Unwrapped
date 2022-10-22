package circefx

import cats._
import cats.data._
import cats.implicits._
import fx.{given, _}
import io.circe._
import io.circe.parser._

object Circefx:

  def decodeTest[E, A](c: io.circe.HCursor)(
      using d: Decoder[A],
      cx: Control[NonEmptyList[E] | DecodingFailure]): A =
    d(c).bind

  def parsing[E, A](s: String)(
      using cx: Control[NonEmptyList[E] | ParsingFailure | DecodingFailure],
      d: Decoder[A]): A =
    parse(s).bind.as[A].bind
