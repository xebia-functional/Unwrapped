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

def streamed[A](f: Unit % Send[A] % Structured): Receive[A] % Structured =
  (receive: (A) => Unit) => {
    val q = new SynchronousQueue[A]()
    val downstream = fork(() => {
      try
        while (true) {
          receive(q.take)
        }
      catch
        case e: InterruptedException => ()
    })
    forkAndComplete(() => {
      given Send[A] = (a: A) => q.put(a)
      f
    }) { (str, fiber) => 
      downstream.cancel(true)
    }
  }

@implicitNotFound(
  "Receiving values from streams or channels require capability:\n% Receive[${A}]"
)
trait Receive[+A]:
  def receive(f: A => Unit): Unit

extension [A](r: Receive[Receive[A]] % Structured)
  def flatten: Receive[A] % Structured =
    streamed(r.receive(sendAll))

  def flattenMerge(
      concurrency: Int
  ): Receive[A] % Structured =
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

extension [A](r: Receive[A] % Structured)

  def transform[B](f: (A => Unit) % Send[B]): Receive[B] % Structured =
    streamed(r.receive(f))

  def filter(predicate: (A => Boolean) % Send[A]): Receive[A] % Structured =
    transform { value => if (predicate(value)) send(value) }

  def map[B](f: (A => B)): Receive[B] % Structured =
    transform { v => send(f(v)) }

  def flatMap[B](transform: A => Receive[B]): Receive[B] % Structured =
    map(transform).flatten

  def flatMapMerge[B](concurrency: Int)(
      transform: A => Receive[B]
  ): Receive[B] % Structured % Send[Receive[B]] % Send[B] =
    map(transform).flattenMerge(concurrency)

  def zipWithIndex: Receive[(A, Int)] % Structured =
    var index = 0
    map { (value: A) =>
      if (index < 0) throw ArithmeticException("Overflow")
      val v = (value, index)
      index = index + 1
      v
    }

  def fold[R](initial: R, operation: (R, A) => R): Receive[R] % Structured =
    streamed {
      var acc: R = initial
      send(acc)
      r.receive { (value: A) =>
        acc = operation(acc, value)
        send(acc)
      }
    }

  def toList: List[A] % Structured =
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

def streamOf[A](values: A*): Receive[A] % Structured =
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

  val listed = structured {
    streamed(sent)
      .transform((n: Int) => send(n + 1))
      .filter((n: Int) => n % 2 == 0)
      .map((n: Int) => n * 10)
      .zipWithIndex
      .toList
  }

  println(listed)
