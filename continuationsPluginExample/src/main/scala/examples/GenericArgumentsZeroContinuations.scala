package examples

import continuations.Suspend

@main def GenericArgumentsZeroContinuations =
  case class Foo(bar: String)
  def genericArgumentsZeroContinuations[A](a: A)(using Suspend): A = a
  println(genericArgumentsZeroContinuations(Foo("bar")))
