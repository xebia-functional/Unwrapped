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
import scala.concurrent.duration._

object CatsEffectTests extends Properties("Cats Effect Tests"):
  property("fx happy programs to IO") = forAll { (a: Int) =>
    val effect: Control[Throwable] ?=> Int = a
    toEffect[IO, Throwable, Int](effect).unsafeRunSync() == a
  }

  property("fx failing programs to ApplicativeError effects") = forAll { (b: String) =>
    val effect: Control[String] ?=> Int = b.shift
    implicit val ae: ApplicativeError[[a] =>> Either[String, a], String] =
      catsStdInstancesForEither
    toEffect[[a] =>> Either[String, a], String, Int](effect) == Left(b)

  }

  property("fx failing throwable programs to IO effects") = forAll { (b: String) =>
    val expectedException = RuntimeException(b)
    val effect: Control[Throwable] ?=> Int = expectedException.shift

    toEffect[IO, Throwable, Int](effect).attempt.unsafeRunSync() == Left(expectedException)

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

  property("fromIO can cancel nested async IOs") = forAll { (i: Int) =>
    IO.async_[Int] { cb =>
      structured {
        val fiber = fromIO(
          IO.async_[Int] { _ => }
            .onCancel(IO {
              cb(Right(i))
            }))
        try fiber.cancel(true)
        catch case e: Throwable => () // ignore blow up
      }
    }.unsafeRunSync() == i
  }
