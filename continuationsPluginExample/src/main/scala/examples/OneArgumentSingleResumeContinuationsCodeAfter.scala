package examples

import continuations.Suspend

@main def OneArgumentSingleResumeContinuationsCodeAfter =
  def oneArgumentSingleResumeContinuationsCodeAfter(str: String)(using s: Suspend): Int =
    s.suspendContinuation(_.resume(Right(println(str))))
    10
  println(oneArgumentSingleResumeContinuationsCodeAfter("Hello"))
