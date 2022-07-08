package fx.sip

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

val effects: Structured ?=> Control[NotFound.type | None.type] ?=> String =
  val jane = fork(() => Person("Jane", Right(Address(Some(Country(Some("ES")))))))
  val joe = fork(() => Person("Joe", Left(NotFound)))
  val janeEffect = getCountryCodeDirect(jane)
  val joeEffect = getCountryCodeDirect(joe)
  run(s"$janeEffect, $joeEffect")

@main def program =
  println(run(structured(effects))) // NotFound
