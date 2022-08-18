package sttp
package fx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite
import sttp.client3.StringBody

import java.net.URI
import java.net.http.HttpResponse

class PatchPartiallyAppliedSuite extends ScalaFXSuite, HttpExtensionsSuiteFixtures:

  FunFixture
    .map2(httpServer(patchHandler), stringBody)
    .testFX("apply[Receive[Byte] should return the correct stream of bytes") {
      case (serverAddressResource, body) =>
        serverAddressResource.use { serverAddressBase =>
          given HttpBodyMapper[StringBody] = body.toHttpBodyMapper()
          val response: Http[HttpResponse[Receive[Byte]]] =
            URI(s"$serverAddressBase/ping").patch[Receive[Byte]](body)
          val result: Http[String] = response.fmap { response =>
            new String(response.body().toList.toArray)
          }
          assertEqualsFX(result.httpValue, Accepted.statusText)
        }
    }
