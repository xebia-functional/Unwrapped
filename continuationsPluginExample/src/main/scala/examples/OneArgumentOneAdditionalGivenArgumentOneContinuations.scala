package examples

import continuations.Suspend

@main def OneArgumentOneAdditionalGivenArgumentOneContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentOneContinuations(
      x: Int)(using Suspend, String): String =
    shift[String] { continuation =>
      continuation.resume(summon[String] + x)
    }
  println(oneArgumentOneAdditionalGivenArgumentOneContinuations(1))
