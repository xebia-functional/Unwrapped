package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CompletableFuture
import org.scalacheck.Test.Parameters
import java.util.concurrent.Semaphore
import scala.util.control.NonFatal
import java.time.Duration

type TestTuple5 = (Int, String, Double, Long, Char)
type TestTuple10 = Tuple.Concat[TestTuple5, TestTuple5]
type TestTuple20 = Tuple.Concat[TestTuple10, TestTuple10]
type TestTupleXXL = Tuple.Concat[TestTuple20, TestTuple20]

object StructuredTests extends Properties("Structured Concurrency Tests"):

  override def overrideParameters(p: Parameters) =
    p.withMinSuccessfulTests(10000)

  property("parallel runs in parallel") = forAll { (a: Int, b: Int) =>
    val r = AtomicReference("")
    val modifyGate = CompletableFuture[Int]()
    structured(
      (
        () =>
          modifyGate.join
          r.updateAndGet { i => s"$i$a" },
        () =>
          r.set(s"$b")
          modifyGate.complete(0)
      ).par[Function0]
    )
    r.get() == s"$b$a"
  }

  property("concurrent shift on fork join propagates") = forAll {
    (a: Int, b: Int) =>
      val x: String % Structured % Control[Int] =
        val fa = fork[String](() => a.shift)
        val fb = fork[String](() => b.shift)
        fa.join + fb.join

      val value: String | Int = run(structured(x))

      List(a, b).contains(value)
  }

  property("concurrent shift on fork that doesn't join does not propagate") =
    forAll { (a: Int, b: Int, c: String) =>
      val x: String % Structured % Control[Int] =
        val fa = fork[Nothing](() => a.shift)
        val fb = fork[Nothing](() => b.shift)
        c

      c == run(structured(x))
    }

  property("tupleXXL par") = forAll { (t20a: TestTuple20, t20b: TestTuple20) =>
    val tupleXXL: TestTupleXXL = t20a ++ t20b
    tupleXXL == run(structured(tupleXXL.par[Id]))
  }

end StructuredTests
