package fx

import java.lang.{Continuation, ContinuationScope}
import java.util.concurrent.CancellationException

inline def fold[R, A, B](inline fc: A |> Control[R])(inline f: R => B, inline g: A => B): B = {
  var result: A | B | Null = null
  new ContinuationScope("scala-fx continuation scope") with Control[R] {

    val cont = new java.lang.Continuation(
      this,
      () => {
        result = g(fc(using this))
      }
    )

    extension (r: R)
      def shift[A]: A = {
        result = f(r)
        Continuation.`yield`(this)
        result.asInstanceOf[A]
      }

    cont.run
  }
  result.asInstanceOf[B]
}
