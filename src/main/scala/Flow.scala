package fx

import java.util.concurrent.Semaphore
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Receiving values from streams or channels require capability:\n* Receive[${A}]"
)
trait Receive[+A]:
  def receive(f: A => Unit): Unit

extension [A](r: Receive[Receive[A]])
  def flatten: Receive[A] =
    streamed(r.receive(sendAll))

  def flattenMerge[B](
      concurrency: Int
  ): Receive[B] * Structured * Send[Receive[A]] =
    val semaphore = Semaphore(concurrency)
    streamed(receive { (inner: Receive[A]) =>
      semaphore.acquire()
      uncancellable(() => {
        try send(inner)
        finally semaphore.release()
      }).join
    }(using r))

extension [A](r: Receive[A])

  def transform[B](f: (A => Unit) * Send[B]): Receive[B] =
    streamed(receive(f)(using r))

  def filter(predicate: (A => Boolean) * Send[A]): Receive[A] =
    transform { value =>
      if (predicate(value)) send(value)
    }

  def map[B](f: (A => B) * Send[B]): Receive[B] =
    transform { v => send(f(v)) }

  def flatMap[B](transform: A => Receive[B]): Receive[B] =
    map(transform).flatten

  def flatMapMerge[B](concurrency: Int)(
      transform: A => Receive[B]
  ): Receive[B] * Structured * Send[Receive[B]] =
    map(transform).flattenMerge(concurrency)

  def indexed: Receive[(Int, A)] =
    var index = 0
    map { (value: A) =>
      index = index + 1
      if (index < 0) throw ArithmeticException("Overflow")
      (index, value)
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

def receive[A](f: A => Unit): Unit * Receive[A] =
  summon[Receive[A]].receive(f)

@implicitNotFound(
  "Sending values to streams or channels require capability:\n* Send[${A}]"
)
trait Send[A]:
  def send(value: A): Unit
  def sendAll(receive: Receive[A]): Unit =
    receive.receive(send)

def send[A](value: A): Unit * Send[A] =
  summon[Send[A]].send(value)

def sendAll[A](receive: Receive[A]): Unit * Send[A] =
  summon[Send[A]].sendAll(receive)

def streamed[A](f: Unit * Send[A]): Receive[A] =
  (receive: (A) => Unit) =>
    given Send[A] = (a: A) => receive(a)
    f

private[this] def repeat(n: Int)(f: (Int) => Unit): Unit =
  for (i <- 0 to n) f(i)

val sent: Unit * Send[Int] =
  repeat(100)(send)

val received: Unit * Receive[(Int, Int)] =
  receive(println)

@main def SimpleFlow: Unit =
  received(using
    streamed(sent)
      .transform((n: Int) => send(n + 1))
      .filter((n: Int) => n % 2 == 0)
      .map((n: Int) => n * 10)
      .indexed
  )
