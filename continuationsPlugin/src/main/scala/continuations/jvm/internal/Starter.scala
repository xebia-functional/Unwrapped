package continuations.jvm.internal

import continuations.Continuation

abstract class Starter:
  def invoke[A](completion: Continuation[A]): A | Any | Null

object Starter:
  given Starter = Marker

object Marker extends Starter:
  override def invoke[A](completion: Continuation[A]): A | Any | Null = ???
