package examples

import continuations.Suspend

@main def OneArgumentOneAdditionalGivenArgumentTwoContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentTwoContinuations(
      x: Int)(using Suspend, String): Int =
    summon[Suspend].suspendContinuation[Unit] { continuation =>
      continuation.resume(Right(println(summon[String] + x)))
    }
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
  println(oneArgumentOneAdditionalGivenArgumentTwoContinuations(1))
