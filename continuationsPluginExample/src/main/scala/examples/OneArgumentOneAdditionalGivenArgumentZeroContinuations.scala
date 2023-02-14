package examples

import continuations.Suspend

@main def OneArgumentOneAdditionalGivenArgumentZeroContinuations =
  given String = "Output: "
  def oneArgumentOneAdditionalGivenArgumentZeroContinuations(
      x: Int)(using s: String, sus: Suspend): String = s + x
  println(oneArgumentOneAdditionalGivenArgumentZeroContinuations(1))
