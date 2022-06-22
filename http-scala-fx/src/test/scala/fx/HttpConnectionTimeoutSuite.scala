package fx

import scala.compiletime.testing.typeChecks
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.MINUTES

import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class HttpConnectionTimeoutSuite extends ScalaCheckSuite {

  property("Any positive long can be a HttpConnectionTimeout") {
    forAll { (i: Long) =>
      (i > 0) ==> {
        run(HttpConnectionTimeout.of(i)) match {
          case s: String => fail(s"unexpected shift to error: $s")
          case _ => passed
        }
      }
      (i <= 0) ==> {
        run(HttpConnectionTimeout.of(i)) match {
          case s: String => s == "Durations must be positive"
          case _ =>
            fail("0 or negative numbers should have failed")
        }
      }
    }
  }
}
