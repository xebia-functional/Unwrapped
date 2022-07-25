package continuations.examples

import scala.concurrent.Future
import scala.util.{Failure, Success}
import continuations._
import concurrent.ExecutionContext.Implicits.global

def await[A](future: Future[A]): Suspend ?=> A =
  summon[Suspend].continuation[A] { (c: Continuation[A]) =>
    future.onComplete {
      case Success(a) => c.resume(Right(a))
      case Failure(e) => c.resume(Left(e))
    }
  }

def awaitCPS[A](future: Future[A], continuation: Continuation[A]): Any | Null = ???

def a(): Int = 47
def b(): Unit = ()
def c(z: Int): Int = z

def foo(x: Int): Future[Int] = Future(x)
def bar(x: Int, y: Int): Future[Int] = Future(x + y)

def program: Suspend ?=> Int =
  val x: Int = a()
  val y: Int = await(foo(x)) // suspension point #1
  b()
  val z = await(bar(x, y)) // suspension point #2
  c(z)

def program(var0: Continuation[_]): Any = ???
