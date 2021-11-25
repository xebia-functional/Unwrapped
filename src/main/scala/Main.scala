package whatever

import fx.*
import java.time.Duration
import java.util.concurrent.CancellationException
import cats.syntax.validated

def program2: Int * Bind =
  Right(1).bind + Right(2).bind

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

  val value2: Int =
    run(program2 + program2)

  val threads: Long *: Long *: Long *: String *: Long *: Long *: String *: EmptyTuple = parallel(
    (
      () => Thread.currentThread.getId,
      () => Thread.currentThread.getId,
      () => Thread.currentThread.getId,
      () => "boom",
      () => Thread.currentThread.getId,
      () => Thread.currentThread.getId,
      () => "boom"
    )
  )

  println(threads)
  println(value)
  println(value2)
