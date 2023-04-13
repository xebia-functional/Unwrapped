package examples

import continuations.Suspend

@main def OneArgumentSingleResumeContinuationsCodeAfter =
  def oneArgumentSingleResumeContinuationsCodeAfter(str: String)(using Suspend): Int =
    shift(_.resume(println(str)))
    10
  println(oneArgumentSingleResumeContinuationsCodeAfter("Hello"))
