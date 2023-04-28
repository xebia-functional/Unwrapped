package unwrapped

import munit.unwrapped.UnwrappedSuite

import scala.jdk.CollectionConverters.*
import java.net.URI

class HttpResponseMapperSuite extends UnwrappedSuite, HttpServerFixtures:
  val debug = false
  httpServer(getLoremHandler(None)).testUnwrapped(
    "an http request expecting a Receive byte body should return the body in a stream") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        val bodyAsReceive: unwrapped.Receive[Byte] =
          structured(URI.create(s"$baseServerAddress/stream").GET[Receive[Byte]]()).body
        val bodyAsString = new String(
          bodyAsReceive
            .map { b =>
              if (debug) {
                println(s"in test receive, showing streaming behavior: ${b.toInt.toHexString}")
              }; b
            }
            .toList
            .toArray)
        assertEqualsUnwrapped(bodyAsString, loremIpsumBody)

      }
  }
