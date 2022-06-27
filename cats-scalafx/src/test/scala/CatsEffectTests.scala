package fx
package cats

import _root_.{cats => c}
import c.effect.*
import c.implicits.*
import c.effect.implicits.*
import c.effect.unsafe.implicits.*
import c.syntax.either._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.CancellationException
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

object CatsEffectTests extends Properties("Cats Effect Tests"):
  property("IO happy programs to fx") = forAll { (a: Int, b: Int) =>
    val effect: IO[Int] = IO.pure(a).map(_ + b)
    run(structured(fromIO(effect).join)) == a + b
  }

  property("IO failing programs to fx") = forAll { (b: Throwable) =>
    val effect: IO[Int] = IO(throw b)
    val result =
      try run(structured(fromIO(effect).join))
      catch
        case e: Throwable =>
          e
    result == b
  }

  property("IO cancellation is propagated through fx structure") = forAll { (a: Int) =>
    var aa: Int | Null = null
    try
      structured {
        fromIO(
          IO.canceled
            .onCancel(IO {
              aa = a
            }))
      }
      false
    catch
      case e: CancellationException => aa == a
      case e: Throwable =>
        println(e)
        false
  }

  property("fx happy programs to IO") = forAll { (a: Int) =>
    val effect: Control[String] ?=> Int = a
    toCatsEffect[IO, String, Int](effect).unsafeRunSync() == a
  }

  property("fx failing programs with Control[Throwable] to IO") = forAll { (b: String) =>
    val effect: Control[Throwable] ?=> Int = new RuntimeException(b).shift
    toCatsEffect[IO, Throwable, Int](effect).attempt.unsafeRunSync().leftMap { e =>
      e.getMessage
    } == Left(b)
  }

  property("fx failing programs to IO") = forAll { (b: String) =>
    val effect: Control[String] ?=> Int = b.shift
    toCatsEffect[IO, String, Int](effect).attempt.unsafeRunSync() == Left(
      NonThrowableFXToCatsException(b))

  }

  property("fromIO can handle errors through IO") = forAll { (t: Throwable, expected: Int) =>
    structured {
      val actual = fromIO(IO[Int] {
        throw t
      }.handleErrorWith { _ => IO.pure(expected) })
      actual.join == expected
    }
  }

  property("structured cancellation should cancel IO") = forAll { (i: Int) =>
    val promise = CompletableFuture[Int]()
    val latch = CompletableFuture[Unit]()
    structured {
      val fiber = fromIO(IO {
        latch.complete(())
      }.flatMap(_ => IO.never[Int]).onCancel {
        IO(promise.complete(i))
      })
      latch.get()
      try fiber.cancel(true)
      catch case e: Throwable => () // ignore blow up
      promise.get() == i
    }
  }
