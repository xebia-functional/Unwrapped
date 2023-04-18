package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentOneAdditionalGivenArgumentTwoContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentTwoContinuations(
      x: Int)(using Suspend, String): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(summon[String] + x))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(2) }
  println(SuspendApp(oneArgumentOneAdditionalGivenArgumentTwoContinuations(1)))
