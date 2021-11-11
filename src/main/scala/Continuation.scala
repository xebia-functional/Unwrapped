package fx

import java.lang.{Continuation, ContinuationScope}
import java.util.concurrent.CancellationException


private[fx]
class Coroutine[R, A](fc: Effect[R] ?=> A) extends Cont[R, A] {

  def fold[B](f: R => B, g: A => B): B = {
    var result : A | B | Null = null 
    val effect = new ContinuationScope("scala-fx continuation scope") with Effect[R] {
        val cont = new java.lang.Continuation(this, () => { 
            result = g(fc(using this))
        })
    
        override def shift[A](r: R): A = {
            result = f(r)
            Continuation.`yield`(this)
            result.asInstanceOf[A]
        }

        cont.run
    }
    result.asInstanceOf[B]
  }
}

