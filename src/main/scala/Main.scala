package whatever

import fx.*
import java.time.Duration
import java.util.concurrent.CancellationException
import cats.syntax.validated


def program: Int
  * Bind
  * Errors[String] =
  Left[String, Int]("boom").bind +
    Right(1).bind +
    Right(2).bind +
    "boom1".raise[Int]

@main def hello() =
  import fx.runtime

  val value: Int | String =
    run(program + program)

  val results: TupledVarargs[[R] =>> () => R, (() => String, () => Int, () => Double)]#Result = parallel(
    (
      () => "1",
      () => 0,
      () => 47.0
    )
  )

  println(results)
  println(value)
