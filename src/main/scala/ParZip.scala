package fx

import scala.annotation.implicitNotFound
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable

@implicitNotFound(
  "Parallel zip requires capability:\n* ParZip"
)
opaque type ParZip = Unit

object ParZip:
  given ParZip = ()

object Pool:
  private val pool = Executors.newVirtualThreadExecutor()
  inline def await[A](inline f: => A): A =
    println(s"scheduling in Thread ${Thread.currentThread.getId}")
    pool
      .submit(new Callable[A] {
        def call() =
          println(s"running in Thread ${Thread.currentThread.getId}")
          f
      })
      .join

def parMap[A, B, C](left: => A, right: => B, f: (A, B) => C): C * ParZip =
  val a = Pool.await(left)
  val b = Pool.await(right)
  f(a, b)
