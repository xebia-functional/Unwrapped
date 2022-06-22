package fx

import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class HttpRetriesSuite extends ScalaCheckSuite {

  property("Any positive int can be a HttpRetries") {
    forAll { (i: Int) =>
      (i > 0) ==> {
        run(HttpRetries.of(i)) match {
          case s: String => fail(s"unexpected shift to error: $s")
          case _ => passed
        }
      }
      (i <= 0) ==> {
        run(HttpRetries.of(i)) match {
          case s: String => s == "HttpRetries must be a positive Number"

          case _ =>
            fail("0 or negative numbers should have failed")
        }
      }
    }
  }
}
