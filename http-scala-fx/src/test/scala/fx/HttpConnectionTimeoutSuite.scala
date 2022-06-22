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
        HttpConnectionTimeout.of(i).right.get.toLong == i
      }
      (i <= 0) ==> { HttpConnectionTimeout.of(i) == Left("Durations must be positive") }
    }
  }
}
