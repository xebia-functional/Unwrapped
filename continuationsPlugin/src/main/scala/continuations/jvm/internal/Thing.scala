package continuations.jvm.internal

import continuations.Continuation

abstract class Thing:
  def invoke(completion: Continuation[Int]): Int | Any | Null

object Thing:
  given Thing = Marker
  
object Marker extends Thing:
  override def invoke(completion: Continuation[Int]): Int | Any | Null = ???
