package fx

import munit.ScalaCheckSuite
import org.scalacheck.Prop._

import scala.annotation.nowarn

class HttpConnectionTimeoutSuite extends ScalaCheckSuite:

  property("Any positive long can be a HttpConnectionTimeout") {
    forAll { (i: Long) =>
      (i > 0) ==> {
        @nowarn // value will not resolve without the fruitless typecheck
        val result = run(HttpConnectionTimeout.of(i)) match
          case s: String => fail(s"unexpected shift to error: $s")
          case x: HttpConnectionTimeout => x.value == i
        result
      }
      (i <= 0) ==> {
        run(HttpConnectionTimeout.of(i)) match
          case s: String => s == "Durations must be positive"
          case _ =>
            fail("0 or negative numbers should have failed")
      }
    }
  }
