package continuations.jvm.internal

import continuations.Continuation

abstract class Starter:
  def invoke(completion: Continuation[Int]): Int | Any | Null

object Starter:
  given Starter = Marker

object Marker extends Starter:
  override def invoke(completion: Continuation[Int]): Int | Any | Null = ???
