package continuations

sealed trait Suspend:
  def continuation[A](f: Continuation[A] => Unit): A

object Suspend:
  given Suspend = Marker

object Marker extends Suspend:
  override def continuation[A](f: Continuation[A] => Unit): A =
    ???
