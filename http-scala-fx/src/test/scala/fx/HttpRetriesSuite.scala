package fx

import munit.ScalaCheckSuite
import org.scalacheck.Prop._

import scala.annotation.nowarn

class HttpRetriesSuite extends ScalaCheckSuite:

  property("Any positive int can be a HttpRetries") {
    forAll { (i: Int) =>
      (i >= 0) ==> {
        @nowarn
        val result = run(HttpRetries.of(i)) match
          case ex: HttpExecutionException => fail(s"unexpected raise to error: $ex.getMessage")
          case x: HttpRetries => x.value == i
        result
      }
      (i < 0) ==> {
        run(HttpRetries.of(i)) match
          case ex: HttpExecutionException =>
            ex.getMessage == "HttpRetries must be greater than 0"

          case _ =>
            fail("negative numbers should have failed")
      }
    }
  }
