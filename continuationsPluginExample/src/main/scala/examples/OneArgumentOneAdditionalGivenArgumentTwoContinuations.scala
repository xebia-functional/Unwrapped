package examples

import continuations.Suspend

@main def OneArgumentOneAdditionalGivenArgumentTwoContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentTwoContinuations(
      x: Int)(using Suspend, String): Int =
    shift[Unit](continuation => continuation.resume(println(summon[String] + x)))
    shift[Int] { continuation => continuation.resume(2) }
  println(oneArgumentOneAdditionalGivenArgumentTwoContinuations(1))
