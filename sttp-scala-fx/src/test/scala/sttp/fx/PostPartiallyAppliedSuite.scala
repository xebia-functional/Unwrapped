package sttp
package fx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite
import sttp.client3.StringBody

import java.net.URI

class PostPartiallyAppliedSuite extends ScalaFXSuite, HttpExtensionsSuiteFixtures:

  FunFixture
    .map2(httpServer(postHandler), stringBody)
    .testFX("URI#post[Receive[Byte]](stringBody) should return as a stream of bytes") {
      case (serverBaseAddressResource, stringBody) =>
        serverBaseAddressResource.use { serverBaseAddress =>
          given HttpBodyMapper[StringBody] = stringBody.toHttpBodyMapper()
          assertEqualsFX(
            URI(s"$serverBaseAddress/ping")
              .post[Receive[Byte]](stringBody)
              .fmap { response => new String(response.body().toList.toArray) }
              .httpValue,
            Created.statusText
          )
        }
    }
