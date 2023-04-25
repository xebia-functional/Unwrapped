package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentOneAdditionalGivenArgumentOneContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentOneContinuations(
      x: Int)(using Suspend, String): String =
    summon[Suspend].shift[String] { continuation =>
      continuation.resume(summon[String] + x)
    }
  println(SuspendApp(oneArgumentOneAdditionalGivenArgumentOneContinuations(1)))
