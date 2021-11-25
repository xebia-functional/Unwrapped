package fx

import jdk.internal.vm.{Continuation, ContinuationScope}
import java.util.concurrent.CancellationException
import scala.annotation.implicitNotFound
import scala.util.control.ControlThrowable
import java.util.UUID
import java.util.concurrent.ExecutionException
import scala.util.control.NonFatal

private[this] def fold2[R, A, B](
    program: A * Control[R]
)(f: R => B, g: A => B): B = {
  var result: A | B | Null = null
  new ContinuationScope("scala-fx continuation scope") with Control[R] {

    private[fx] val token: String = UUID.randomUUID.toString

    val cont = new jdk.internal.vm.Continuation(
      this,
      () => {
        given Control[R] = this
        result = g(program(using this))
      }
    )

    extension (r: R)
      def shift[A]: A = {
        result = f(r)
        Continuation.`yield`(this) //suspend non blocking
        result.asInstanceOf[A]
      }

    cont.run
  }
  result.asInstanceOf[B]
}

private[this] inline def fold[R, A, B](
    inline program: A * Control[R]
)(inline recover: R => B, inline transform: A => B): B = {
  var result: Any | Null = null
  val control = new Control[R] {

    private[fx] val token: String = UUID.randomUUID.toString

    extension (r: R)
      def shift[A]: A =
        throw ControlToken(token, r, recover.asInstanceOf[Any => Any])

  }
  try {
    given Control[R] = control
    result = transform(program(using control))
  } catch {
    case e: Throwable =>
      result = handleControl(control, e)
  }
  result.asInstanceOf[B]
}

def handleControl(control: Control[_], e: Throwable): Any =
  e match
    case e: ExecutionException =>
      handleControl(control, e.getCause)
    case e @ ControlToken(token, shifted, recover) =>
      if (control.token == token)
        recover(shifted)
      else
        throw e

private case class ControlToken(
    token: String,
    shifted: Any,
    recover: (Any) => Any
) extends ControlThrowable
