package sttp
package unwrapped

import _root_.unwrapped.{given, *}
import munit.unwrapped.UnwrappedSuite
import sttp.client3.StringBody

import java.net.URI
import java.net.http.HttpResponse
import scala.concurrent.duration.*
import scala.language.postfixOps

class PatchPartiallyAppliedSuite extends UnwrappedSuite, HttpExtensionsSuiteFixtures:

  FunFixture
    .map2(httpServer(patchHandler), stringBody)
    .testUnwrapped("apply[Receive[Byte] should return the correct stream of bytes") {
      case (serverAddressResource, body) =>
        serverAddressResource.use { serverAddressBase =>
          given HttpBodyMapper[StringBody] = body.toHttpBodyMapper()
          val response: Http[HttpResponse[Receive[Byte]]] =
            URI(s"$serverAddressBase/ping").patch[Receive[Byte]](body, 5 seconds)
          val result: Http[String] = response.fmap { response =>
            new String(response.body().toList.toArray)
          }
          assertEqualsUnwrapped(result.httpValue, Accepted.statusText)
        }
    }
