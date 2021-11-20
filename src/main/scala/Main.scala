package whatever

import fx._
import java.time.Duration

def program2: Int * Bind =
  Right(1).bind +
    Right(2).bind

def program: Int
  * Control[String]
  * Bind
  * Errors =
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

  val threads = parMap(
    {
      Thread.sleep(Duration.ofSeconds(1))
      throw RuntimeException("Boom")
      Thread.currentThread.getId
    }, {
      Thread.sleep(Duration.ofSeconds(2))
      println(
        "should not reach this as it should be cancelled because of the other ex"
      )
      Thread.currentThread.getId
    },
    (a, b) => (a, b)
  )

  println(value)
  println(value2)
  println(threads)
