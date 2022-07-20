package fx

import java.util.concurrent.Semaphore
import scala.annotation.implicitNotFound
import scala.collection.mutable.ListBuffer

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
    streamed(r.receive { (inner: Receive[A]) =>
      semaphore.acquire()
      uncancellable(() => {
        try sendAll(inner)
        finally semaphore.release()
      })
    })

extension [A](r: Receive[A])

  def transform[B](f: Send[B] ?=> (A => Unit)): Receive[B] =
    streamed(receive(f)(using r))

  def filter(predicate: Send[A] ?=> A => Boolean): Receive[A] =
    transform { value => if (predicate(value)) send(value) }

  def map[B](f: A => B): Receive[B] =
    transform { v => send(f(v)) }

  def flatMap[B](transform: A => Receive[B]): Receive[B] =
    map(transform).flatten

  def flatMapMerge[B](concurrency: Int)(
      transform: A => Receive[B]
  )(using Structured): Receive[B] =
    map(transform).flattenMerge(concurrency)

  def zipWithIndex: Receive[(A, Int)] =
    var index = 0
    map { (value: A) =>
      if (index < 0) throw ArithmeticException("Overflow")
      val v = (value, index)
      index = index + 1
      v
    }

  def grouped(n: Int): Receive[Vector[A]] = { // this could be written
                                            // as a tail rec function,
                                            // but it is unlikely the
                                            // streamed body will be
                                            // extracted and thus from
                                            // the outside is RT.
    streamed {
      var acc: Vector[A] = Vector.empty[A]
      r.receive((value: A) => if(acc.size < n){
        acc = acc :+ value
      } else{
        send(acc)
        acc = Vector(value)
      })
      send(acc)
    }
  }

  def fold[R](initial: R, operation: (R, A) => R): Receive[R] =
    streamed {
      var acc: R = initial
      r.receive { (value: A) => acc = operation(acc, value) }
      send(acc)
    }

  def toList: List[A] =
    val buffer = new ListBuffer[A]
    r.receive(buffer.addOne)
    buffer.toList

def receive[A](f: A => Unit)(using r: Receive[A]): Unit =
  r.receive(f)

@implicitNotFound(
  "Sending values to streams or channels require capability:\n% Send[${A}]"
)
trait Send[A]:
  def send(value: A): Unit
  def sendAll(receive: Receive[A]): Unit =
    receive.receive(send)

def send[A](value: A)(using s: Send[A]): Unit =
  s.send(value)

def sendAll[A](receive: Receive[A])(using s: Send[A]): Unit =
  s.sendAll(receive)

def streamed[A](f: Send[A] ?=> Unit): Receive[A] =
  (receive: (A) => Unit) =>
    given Send[A] = (a: A) => receive(a)
    f

def streamOf[A](values: A*): Receive[A] =
  streamed {
    for (value <- values) send(value)
  }

private[this] def repeat(n: Int)(f: (Int) => Unit): Unit =
  for (i <- 0 to n) f(i)

val source: Send[Int] ?=> Unit =
  repeat(100)(send)

@main def SimpleFlow: Unit =

  val listed = streamed(source)
    .transform((n: Int) => send(n + 1))
    .filter((n: Int) => n % 2 == 0)
    .map((n: Int) => n * 10)
    .zipWithIndex
    .toList

  println(listed)
