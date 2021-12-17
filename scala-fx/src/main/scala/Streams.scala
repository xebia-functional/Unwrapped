package fx

import java.util.concurrent.Semaphore
import scala.annotation.implicitNotFound
import scala.collection.mutable.ListBuffer
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CancellationException
import java.util.concurrent.StructuredExecutor

opaque type ConcurrentQueue[A] = SynchronousQueue[A]

class StructuredSynchronousQueue[A](s: Structured, fair: Boolean = false)
    extends SynchronousQueue[A](fair) {

  private val stack: AtomicReference[List[Fiber[A]]] = AtomicReference(Nil)

  var isClosing = false
  override def take(): A =
    given Structured = s
    val x = fork(() => super.take())
    stack.updateAndGet(_ :+ x)
    x.join

  def shutdown(): Unit =
    given Structured = s
    isClosing = true
    stack.get.foreach((f: Fiber[A]) => f.cancel(true))
}

def streamed[A](f: Unit % Send[A] % Structured): Receive[A] =
  (receive: (A) => Unit) => {
    val scope = StructuredExecutor.open("Scala Fx Stream Scope")
    given Structured = scope.asInstanceOf[Structured]
    val q = new SynchronousQueue[A]()
    given Send[A] = (a: A) => q.put(a)

    val downstream = fork(() => {
      try {
        while (!Thread.currentThread.isInterrupted) {
          receive(q.take())
        }
      } catch 
        case e: InterruptedException => 
          //e.printStackTrace
    })
    forkAndComplete(() => {
      f
    }) { (str, fiber) => downstream.cancel(true) }

    scope.join
    scope.close
  }

@implicitNotFound(
  "Receiving values from streams or channels require capability:\n% Receive[${A}]"
)
trait Receive[+A]:
  def receive(f: A => Unit): Unit

extension [A](r: Receive[Receive[A]])
  def flatten: Receive[A] =
    streamed(r.receive(sendAll))

  def flattenMerge(
      concurrency: Int
  ): Receive[A] =
    val semaphore = Semaphore(concurrency)
    streamed {
      r.receive((inner: Receive[A]) => {
        semaphore.acquire()
        fork(() => {
          try sendAll(inner)
          finally semaphore.release()
        })
      })
    }

extension [A](r: Receive[A])

  def transform[B](f: (A => Unit) % Send[B]): Receive[B] =
    streamed(receive(f)(using r))

  def filter(predicate: (A => Boolean) % Send[A]): Receive[A] =
    transform { value => if (predicate(value)) send(value) }

  def map[B](f: (A => B)): Receive[B] =
    transform { v => send(f(v)) }

  def flatMap[B](transform: A => Receive[B]): Receive[B] =
    map(transform).flatten

  def flatMapMerge[B](concurrency: Int)(
      transform: A => Receive[B]
  ): Receive[B] % Structured % Send[Receive[B]] % Send[B] =
    map(transform).flattenMerge(concurrency)

  def zipWithIndex: Receive[(A, Int)] =
    var index = 0
    map { (value: A) =>
      if (index < 0) throw ArithmeticException("Overflow")
      val v = (value, index)
      index = index + 1
      v
    }

  def fold[R](initial: R, operation: (R, A) => R): Receive[R] =
    streamed {
      var acc: R = initial
      send(acc)
      r.receive { (value: A) =>
        acc = operation(acc, value)
        send(acc)
      }
    }

  def toList: List[A] =
    val buffer = new ListBuffer[A]
    r.receive(buffer.addOne)
    buffer.toList

def receive[A](f: A => Unit): Unit % Receive[A] =
  summon[Receive[A]].receive(f)

@implicitNotFound(
  "Sending values to streams or channels require capability:\n% Send[${A}]"
)
@FunctionalInterface
trait Send[A]:
  def send(value: A): Unit
  def sendAll(receive: Receive[A]): Unit =
    receive.receive(send)

def send[A](value: A): Unit % Send[A] =
  summon[Send[A]].send(value)

def sendAll[A](receive: Receive[A]): Unit % Send[A] =
  summon[Send[A]].sendAll(receive)

def streamOf[A](values: A*): Receive[A] =
  streamed {
    for (value <- values) send(value)
  }

private[this] def repeat(n: Int)(f: (Int) => Unit): Unit =
  for (i <- 0 to n) f(i)

val sent: Unit % Send[Int] =
  repeat(100)(send)

val received: Unit % Receive[(Int, Int)] =
  receive(println)

@main def SimpleFlow: Unit =

  val listed = streamed(sent)
    .transform((n: Int) => send(n + 1))
    .filter((n: Int) => n % 2 == 0)
    .map((n: Int) => n * 10)
    .zipWithIndex
    .toList

  println(listed)
