package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def GenericArgumentsZeroContinuations =
  case class Foo(bar: String)
  def genericArgumentsZeroContinuations[A](a: A)(using Suspend): A = a
  println(SuspendApp(genericArgumentsZeroContinuations(Foo("bar"))))
