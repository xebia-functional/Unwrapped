package sttp
package fx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite
import sttp.client3.StringBody
import scala.concurrent.duration.*

import java.net.URI
import scala.language.postfixOps

class PutPartiallyAppliedSuite extends ScalaFXSuite, HttpExtensionsSuiteFixtures {

  FunFixture
    .map2(httpServer(putHandler), stringBody)
    .testFX("URI#put[Receive[Byte]](body) should return a the correct stream of bytes") {
      case (serverAddressResource, body) =>
        serverAddressResource.use { serverAddressBase =>
          given HttpBodyMapper[StringBody] = body.toHttpBodyMapper()
          assertEqualsFX(
            URI(s"$serverAddressBase/ping")
              .put[Receive[Byte]](body, 5 seconds)
              .fmap { response => new String(response.body.toList.toArray) }
              .httpValue,
            Accepted.statusText
          )
        }
    }

}
