package continuations.example

sealed trait Suspended
object Marker extends Suspended

object Suspended:
  given Suspended = Marker

def foo: Suspended ?=> String =
  "foo"

def bar: Suspended ?=> String =
  "bar"

def program: Suspended ?=> String =
  foo + bar

@main def main =
  println(program)
