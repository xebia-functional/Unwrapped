package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentOneAdditionalGivenArgumentZeroContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentZeroContinuations(
      x: Int)(using s: String, sus: Suspend): String = s + x
  println(SuspendApp(oneArgumentOneAdditionalGivenArgumentZeroContinuations(1)))
