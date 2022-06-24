package fx
package cats

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import _root_.cats.effect.*
import _root_.cats.effect.unsafe.implicits.*

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import scala.concurrent.CancellationException
import scala.concurrent.duration.{Duration, FiniteDuration}

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
        fromIO(IO.canceled.onCancel(IO {
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
    toIO(effect).unsafeRunSync() == a
  }

  property("fx failing programs to IO") = forAll { (b: String) =>
    val effect: Control[String] ?=> Int = b.shift
    toIO(effect).unsafeRunSync() == b
  }
