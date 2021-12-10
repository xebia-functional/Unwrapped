package whatever

import fx.*
import java.time.Duration
import java.util.concurrent.CancellationException
import cats.syntax.validated

val program: Int
  * Errors[String] =
  Left[String, Int]("boom").bind +
    Right(1).bind +
    Right(2).bind +
    "boom1".raise[Int]

@main def hello() =
  import fx.runtime

  val value: Int | String =
    run(program + program)

  val results : (String, Int, Double) * Structured = 
    parallel(
      (
        () => "1",
        () => 0,
        () => 47.0
      )
    )

  println(structured(results))
  println(value)
