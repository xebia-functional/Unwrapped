package fx

import munit.fx.ScalaFXSuite
import java.net.http.HttpResponse.ResponseInfo

import java.net.http.HttpResponse
import java.net.http.HttpHeaders
import java.net.http.HttpClient.Version
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.Executors
import java.nio.charset.StandardCharsets

import scala.jdk.CollectionConverters.*
import java.net.URI

class HttpResponseMapperSuite extends ScalaFXSuite, HttpServerFixtures {
  httpServer(getLoremHandler(None)).testFX(
    "an http request expecting a Receive byte body should return the body in a stream") {
    serverAddressResource =>
      serverAddressResource.use { baseServerAddress =>
        val bodyAsReceive: fx.Receive[Byte] =
          structured(
            Http.GET[Receive[Byte]](URI.create(s"$baseServerAddress/stream")).join.body)
        val bodyAsString = new String(
          bodyAsReceive
            .map { b =>
              println(s"in test receive, showing streaming behavior: ${b.toInt.toHexString}"); b
            }
            .toList
            .toArray)
        // val bodyAsString = new String(bodyAsReceive.toList.toArray, StandardCharsets.UTF_8)
        assertEqualsFX(bodyAsString, loremIpsumBody)

      }
  }
}
