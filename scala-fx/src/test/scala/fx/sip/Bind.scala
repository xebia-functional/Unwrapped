package fx.sip

import java.util.concurrent.{Callable, Executors, TimeUnit}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

extension [A](f: Future[A])
  def bind: A = // since we don't have continuations yet we have to block in async ops
    Await.result(f, scala.concurrent.duration.Duration.Inf)
// otherwise we could have written something like
//    continuation[A] { cont: Continuation[A] => {
//        f.whenComplete { (result, exception) =>
//          if (exception == null) // the future has been completed normally
//            cont.resume(result)
//          else // the future has completed with an exception
//            cont.resumeWithException(exception)
//        }
//      }
//    }
  def apply(): A = bind

extension [R, A](fa: Either[R, A])(using Control[R])
  def bind: A = fa.fold(_.shift, identity)
  def apply(): A = bind

extension [A](fa: Option[A])(using Control[None.type])
  def bind: A = fa.fold(None.shift)(identity)
  def apply(): A = bind

trait Continuation[A]:
  def resume(a: A): Unit
  def resumeWithException(e: Throwable): Unit

// compiler generated implementation of Continuation

suspend def continuation[A](c: Continuation[A] => Unit): A =
  ???

private val executor = Executors.newSingleThreadScheduledExecutor((runnable: Runnable) => {
  val thread = Thread(runnable, "scheduler")
  thread.setDaemon(true)
  thread
})

suspend def delay(time: Long): Unit = continuation { c =>
  val task = new Runnable:
    override def run(): Unit = c.resume(())
  executor.schedule(task, time, TimeUnit.MILLISECONDS)
}
