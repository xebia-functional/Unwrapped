package fx.sip

import java.util.UUID
import java.util.concurrent.ExecutionException
import scala.annotation.tailrec
import scala.util.control.ControlThrowable

object Continuation:
  inline def fold[R, A, B](
      inline program: Control[R] ?=> A
  )(inline recover: R => B, inline transform: A => B): B = {
    var result: Any | Null = null
    implicit val control = new Control[R] {
      val token: String = UUID.randomUUID.toString

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
