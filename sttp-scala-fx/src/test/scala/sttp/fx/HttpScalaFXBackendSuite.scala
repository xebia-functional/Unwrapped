package sttp
package fx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite
import sttp.capabilities.Effect
import sttp.client3._
import sttp.model.Method
import sttp.model.Uri

import java.io.File
import java.net.URI
import java.net.http.HttpTimeoutException
import scala.concurrent.duration.*
import scala.language.postfixOps

import StatusCodeToStatusCode.given

class HttpScalaFXBackendSuite extends ScalaFXSuite, FullBackendFixtures {

  testBody.testFX("string POST to /echo should echo back as a string") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)

    val serverAddress = getServerAddress()
    val result = basicRequest
      .post(Uri(new URI(s"${serverAddress}echo")))
      .body(body)
      .response(asString)
      .send[Http, ReceiveStreams](HttpScalaFXBackend())
      .httpValue
      .body
      .bind
    assertEqualsFX(
      result,
      body
    )
  }

  testBody.testFX("string POST to /echo should echo back as a stream") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)

    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .post(Uri(new URI(s"${serverAddress}echo")))
        .body(body)
        .response(asStream[Http, Receive[Byte], ReceiveStreams](new ReceiveStreams {})(
          (resp: Receive[Byte]) => Http(resp)))
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .body
        .bind
        .map(_.toChar)
        .toList
        .mkString,
      body
    )
  }

  file.testFX("get image should work") { imageFileResource =>
    val serverAddress = getServerAddress()
    val imageAsString = imageFileResource.use { imageFileToWriteTo =>
      val response: File = basicRequest
        .get(Uri(new URI(s"${serverAddress}47DegLogo.svg")))
        .header("Accepts", MediaTypes.image.`svg+xml`.value)
        .response(asFile(imageFileToWriteTo))
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .body
        .bind
      structured {
        fork(() => while (imageFileToWriteTo.getTotalSpace() == 0) {}).join
        fork(() => scala.io.Source.fromFile(imageFileToWriteTo).getLines().mkString).join
      }
    }

    val expected = scala.io.Source.fromResource("brand.svg").getLines().mkString
    assertEqualsFX(imageAsString, expected)
  }

  testBodyAndFile.testFX("string POST to /echo should echo back as a file") {
    case (body, fileResource) =>
      val serverAddress = getServerAddress()
      val fileContents = fileResource.use { file =>
        basicRequest
          .post(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asFile(file))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
          .bind
        structured {
          fork(() => while (file.getTotalSpace() == 0) {}).join
          fork(() => scala.io.Source.fromFile(file).getLines.mkString).join
        }
      }
      assertEqualsFX(fileContents, body)
  }

  testBody.testFX("string POST to /echo should echo back as a byteArray") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      new String(
        basicRequest
          .post(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asByteArray)
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
          .bind),
      body
    )
  }

  testBody.testFX("string POST to /echo should echo back as a both byteArray and string") {
    body =>
      given config: HttpClientConfig =
        HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
      val serverAddress = getServerAddress()
      val (Right(byteArray), Right(str)) =
        basicRequest
          .post(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asBoth(asByteArray, asString))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
      assertEqualsFX((new String(byteArray), str), ("test", "test"))
  }

  testBody.testFX("string PUT to /echo should echo back as a string") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .put(Uri(new URI(s"${serverAddress}echo")))
        .body(body)
        .response(asString)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .body
        .bind,
      body
    )
  }

  testBody.testFX("string PUT to /echo should echo back as a stream") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .put(Uri(new URI(s"${serverAddress}echo")))
        .body(body)
        .response(asStream[Http, Receive[Byte], ReceiveStreams](new ReceiveStreams {})(
          (resp: Receive[Byte]) => Http(resp)))
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .body
        .bind
        .map(_.toChar)
        .toList
        .mkString,
      body
    )
  }

  testBodyAndPutFile.testFX("string PUT to /echo should echo back as a file") {
    case (body, fileResource) =>
      val serverAddress = getServerAddress()
      val fileContents =
        fileResource.use { file =>
          basicRequest
            .put(Uri(new URI(s"${serverAddress}echo")))
            .body(body)
            .response(asFile(file))
            .send[Http, ReceiveStreams](HttpScalaFXBackend())
            .httpValue
            .body
            .bind
          structured {
            fork(() => while (file.getTotalSpace() == 0) {}).join
            fork(() => scala.io.Source.fromFile(file).getLines.mkString).join
          }
        }
      assertEqualsFX(fileContents, body)
  }

  testBody.testFX("string PUT to /echo should echo back as a byteArray") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      new String(
        basicRequest
          .put(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asByteArray)
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
          .bind),
      body
    )
  }

  testBody.testFX("string PUT to /echo should echo back as a both byteArray and string") {
    body =>
      given config: HttpClientConfig =
        HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
      val serverAddress = getServerAddress()
      val (Right(byteArray), Right(str)) =
        basicRequest
          .put(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asBoth(asByteArray, asString))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
      assertEqualsFX((new String(byteArray), str), ("test", "test"))
  }

  testBody.testFX("string PATCH to /echo should echo back as a string") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .patch(Uri(new URI(s"${serverAddress}echo")))
        .body(body)
        .response(asString)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .body
        .bind,
      body
    )
  }

  testBody.testFX("string PATCH to /echo should echo back as a stream") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .patch(Uri(new URI(s"${serverAddress}stream")))
        .body(body)
        .response(asStream[Http, Receive[Byte], ReceiveStreams](new ReceiveStreams {})(
          (resp: Receive[Byte]) => Http(resp)))
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .body
        .bind
        .map(_.toChar)
        .toList
        .mkString,
      body
    )
  }

  testBodyAndPatchFile.testFX("string PATCH to /echo should echo back as a file") {
    case (body, fileResource) =>
      val serverAddress = getServerAddress()
      val fileContents =
        fileResource.use { file =>
          basicRequest
            .patch(Uri(new URI(s"${serverAddress}echo")))
            .body(body)
            .response(asFile(file))
            .send[Http, ReceiveStreams](HttpScalaFXBackend())
            .httpValue
            .body
            .bind
          structured {
            fork[Unit](() => while (file.getTotalSpace() == 0) {}).join
            fork(() => scala.io.Source.fromFile(file).getLines.mkString).join
          }
        }
      assertEqualsFX(fileContents, body)
  }

  testBody.testFX("string PATCH to /echo should echo back as a byteArray") { body =>
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      new String(
        basicRequest
          .patch(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asByteArray)
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
          .bind),
      body
    )
  }

  testBody.testFX("string PATCH to /echo should echo back as a both byteArray and string") {
    body =>
      given config: HttpClientConfig =
        HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
      val serverAddress = getServerAddress()
      val (Right(byteArray), Right(str)) =
        basicRequest
          .patch(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asBoth(asByteArray, asString))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
      assertEqualsFX((new String(byteArray), str), ("test", "test"))
  }

  testFX("delete to /toDelete should return a request with no body") {
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .delete(Uri(new URI(s"${serverAddress}toDelete")))
        .response(ignore)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .code
        .toStatusCode,
      OK
    )
  }

  testFX("HEAD to / should return a request with no body") {
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .head(Uri(new URI(s"${serverAddress}toDelete")))
        .response(ignore)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .code
        .toStatusCode,
      OK
    )
  }

  testFX("OPTIONS to / should return a request with no body") {
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .options(Uri(new URI(s"${serverAddress}toDelete")))
        .response(ignore)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .code
        .toStatusCode,
      OK
    )
  }

  testFX("TRACE to / should return a request with no body") {
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    assertEqualsFX(
      basicRequest
        .method(Method.TRACE, Uri(new URI(s"${serverAddress}toDelete")))
        .response(ignore)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .code
        .toStatusCode,
      OK
    )
  }

  testFX("get to an endpoint that times out should timeout") {
    given config: HttpClientConfig =
      HttpClientConfig(Nullable(HttpConnectionTimeout.of(5)), Nullable.none, Nullable.none)
    val serverAddress = getServerAddress()
    val x = run(
      basicRequest
        .get(Uri(new URI(s"${serverAddress}shouldTimeout")))
        .response(ignore)
        .readTimeout(5 seconds)
        .send[Http, ReceiveStreams](HttpScalaFXBackend())
        .httpValue
        .code
        .toStatusCode
    )
    assertFX(x.isInstanceOf[HttpTimeoutException])
  }

}
