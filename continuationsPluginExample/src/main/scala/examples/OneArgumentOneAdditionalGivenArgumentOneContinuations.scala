package examples

import continuations.Suspend

@main def OneArgumentOneAdditionalGivenArgumentOneContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentOneContinuations(
      x: Int)(using Suspend, String): String =
    summon[Suspend].suspendContinuation[String] { continuation =>
      continuation.resume(Right(summon[String] + x))
    }
  println(oneArgumentOneAdditionalGivenArgumentOneContinuations(1))
