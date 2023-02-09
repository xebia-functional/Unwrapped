package examples

import continuations.Suspend

@main def GenericArgumentsSingleResumeContinuations =
  case class Foo(x: Int)
  def genericArgumentsSingleResumeContinuations[A](x: A)(using Suspend): A =
    summon[Suspend].suspendContinuation(_.resume(Right(x)))
  println(genericArgumentsSingleResumeContinuations(Foo(1)))
