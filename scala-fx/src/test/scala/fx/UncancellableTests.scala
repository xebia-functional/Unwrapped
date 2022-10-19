package fx

import fx.ResourcesTests.property
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

import java.util.concurrent.{CompletableFuture, CompletionException}

object UncancellableTests extends Properties("Bracket Tests"):

  property("uncancellable identity") = forAll { (n: Int) =>
    uncancellable(() => n) == n
  }

  case class CustomEx(val token: String) extends RuntimeException

  property("uncancellable exception identity") = forAll { (msg: String) =>
    val res =
      try
        uncancellable(() => throw CustomEx(msg))
      catch case CustomEx(msg) => msg

    res == msg
  }

//  "Uncancellable back pressures withTimeoutOrNull" {
//    runBlockingTest {
//      checkAll(Arb.long(50, 100), Arb.long(300, 400)) {
//        a
//        , b ->
//        val start = currentTime
//
//        val n = withTimeoutOrNull(a.milliseconds) {
//          uncancellable {
//            delay(b.milliseconds)
//          }
//        }
//
//        val duration = currentTime - start
//
//        n shouldBe null // timed-out so should be null
//        require((duration) >= b) {
//          "Should've taken longer than $b milliseconds, but took $duration"
//        }
//      }
//    }
//  }

end UncancellableTests

