package sttp
package unwrapped

import _root_.unwrapped.{given, *}
import munit.unwrapped.UnwrappedSuite
import sttp.client3.StringBody
import scala.concurrent.duration.*

import java.net.URI
import scala.language.postfixOps

class PutPartiallyAppliedSuite extends UnwrappedSuite, HttpExtensionsSuiteFixtures {

  FunFixture
    .map2(httpServer(putHandler), stringBody)
    .testUnwrapped("URI#put[Receive[Byte]](body) should return a the correct stream of bytes") {
      case (serverAddressResource, body) =>
        serverAddressResource.use { serverAddressBase =>
          given HttpBodyMapper[StringBody] = body.toHttpBodyMapper()
          assertEqualsUnwrapped(
            URI(s"$serverAddressBase/ping")
              .put[Receive[Byte]](body, 5 seconds)
              .fmap { response => new String(response.body.toList.toArray) }
              .httpValue,
            Accepted.statusText
          )
        }
    }

}
