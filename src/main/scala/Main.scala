package whatever

import fx._

def program: Int |> Control[String] |> Bind =
  val r =
    "Boom".shift[Int] +
      Right(1).bind +
      Right(2).bind
  println("Hello!")
  r

@main def hello() = 
  val value : Int |> Control[String] = 
    program + program

  val r: Int | String = run(value)
  println(r)

