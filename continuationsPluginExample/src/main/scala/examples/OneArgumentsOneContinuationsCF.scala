package examples

import continuations.Suspend

@main def OneArgumentsOneContinuationsCF =
  def oneArgumentsOneContinuationsCF(x: Int): Suspend ?=> Int =
    shift { _.resume(x + 1) }
  println(oneArgumentsOneContinuationsCF(1))
