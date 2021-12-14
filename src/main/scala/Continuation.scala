package fx

import scala.annotation.implicitNotFound
import scala.util.control.ControlThrowable
import java.util.UUID
import scala.util.control.NonFatal
import java.util.concurrent.ExecutionException
import scala.annotation.tailrec

object Continuation: 
  inline def fold[R, A, B](
      inline program: A % Control[R]
  )(inline recover: R => B, inline transform: A => B): B = {
    var result: Any | Null = null
    implicit val control = new Control[R] {
      private[fx] val token: String = UUID.randomUUID.toString

      extension (r: R)
        def shift[A]: A =
          throw ControlToken(token, r, recover.asInstanceOf[Any => Any])
    }
    try {
      result = transform(program(using control))
    } catch {
      case e: Throwable =>
        result = handleControl(control, e)
    }
    result.asInstanceOf[B]
  }

  @tailrec def handleControl(control: Control[_], e: Throwable): Any =
    e match
      case e: ExecutionException =>
        handleControl(control, e.getCause)
      case e @ ControlToken(token, shifted, recover) =>
        if (control.token == token)
          recover(shifted)
        else
          throw e
      case _ => throw e

  private case class ControlToken(
      token: String,
      shifted: Any,
      recover: (Any) => Any
  ) extends ControlThrowable
