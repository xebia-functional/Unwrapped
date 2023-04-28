package unwrapped

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import org.scalacheck.Test.Parameters
import java.util.concurrent.Semaphore
import java.util.concurrent.CancellationException
import scala.util.control.NonFatal
import java.time.Duration
import java.time.temporal.TemporalUnit

object UseTests extends Properties("Use Tests"):

  property("finalizer is invoked on success") = forAll { (i: Int, s: String) =>
    val p = CompletableFuture[String]()
    val res = guarantee(
      fa = () => i,
      finalizer = () => p.complete(s)
    )
    p.join == s && res == i
  }

  property("finalizer is invoked on exception") = forAll { (i: Int, s: String) =>
    val p = CompletableFuture[String]()
    val res =
      try
        guarantee(
          fa = () => throw RuntimeException("boom"),
          finalizer = () => p.complete(s)
        )
      catch case e: RuntimeException => i
    p.join == s && res == i
  }

end UseTests
