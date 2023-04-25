package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def GenericArgumentsSingleResumeContinuations =
  case class Foo(x: Int)
  def genericArgumentsSingleResumeContinuations[A](x: A)(using Suspend): A =
    summon[Suspend].shift(_.resume(x))
  println(SuspendApp(genericArgumentsSingleResumeContinuations(Foo(1))))
