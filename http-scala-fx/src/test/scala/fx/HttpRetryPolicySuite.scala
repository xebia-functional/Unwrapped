package fx

import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop._

import java.net.http.HttpResponse

class HttpRetryPolicySuite extends ScalaCheckSuite:

  property(
    "The default retry policy should retry any HttpResponse with a statusCode in the 400s") {
    given arbHttpResponse: Arbitrary[HttpResponse[Any]] = Arbitrary {
      Gen.oneOf(StatusCode.statusCodes).map(StatusCode.unsafeOf).map(FakeHttpResponse[Any](_))
    }
    forAll { (httpResponse: HttpResponse[Any]) =>
      given HttpRetryPolicy = HttpRetryPolicy.defaultRetryPolicy
      val f: (HttpResponse[Any]) => Boolean =
        response => response.shouldRetry
      (httpResponse.statusCode >= 400 && httpResponse.statusCode < 500) ==> {
        f(httpResponse)
      }
      (httpResponse.statusCode < 400 || httpResponse.statusCode > 500) ==> {
        !f(httpResponse)
      }
    }
  }
