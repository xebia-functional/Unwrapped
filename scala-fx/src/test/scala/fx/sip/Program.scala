package fx.sip

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

@main def program =
  val jane = Some(Person("Jane", Future(Right(Address(Some(Country(Some("ES"))))))))
  val joe = Some(Person("Joe", Future(Left(NotFound))))
  val janeEffect: Control[NotFound.type | None.type] ?=> String =
    getCountryCodeDirect(jane)
  val joeEffect: Control[NotFound.type | None.type] ?=> String =
    getCountryCodeDirect(joe)
  val result: NotFound.type | None.type | String = run(s"$janeEffect, $joeEffect")
  println(result) // NotFound
