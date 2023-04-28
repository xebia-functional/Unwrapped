package sttp
package unwrapped

import _root_.unwrapped.{given, *}
import munit.unwrapped.UnwrappedSuite
import sttp.client3.StringBody
import scala.concurrent.duration.*

import java.net.URI
import scala.language.postfixOps

class PostPartiallyAppliedSuite extends UnwrappedSuite, HttpExtensionsSuiteFixtures:

  FunFixture
    .map2(httpServer(postHandler), stringBody)
    .testUnwrapped("URI#post[Receive[Byte]](stringBody) should return as a stream of bytes") {
      case (serverBaseAddressResource, stringBody) =>
        serverBaseAddressResource.use { serverBaseAddress =>
          given HttpBodyMapper[StringBody] = stringBody.toHttpBodyMapper()
          assertEqualsUnwrapped(
            URI(s"$serverBaseAddress/ping")
              .post[Receive[Byte]](stringBody, 5 seconds)
              .fmap { response => new String(response.body().toList.toArray) }
              .httpValue,
            Created.statusText
          )
        }
    }
