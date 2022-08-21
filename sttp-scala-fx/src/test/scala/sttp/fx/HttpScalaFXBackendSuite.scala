package sttp
package fx

import _root_.fx.{given, _}
import munit.fx.ScalaFXSuite
import sttp.capabilities.Effect
import sttp.client3._
import sttp.model.Uri

import java.io.File
import java.net.URI

class HttpScalaFXBackendSuite extends ScalaFXSuite, FullBackendFixtures {

  basicServerAndFile.testFX("get image should work") {
    case (serverAddressResource, imageFileResource) =>
      val imageAsString = serverAddressResource.use { serverAddress =>
        imageFileResource.use { imageFileToWriteTo =>
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
      }
      val expected = scala.io.Source.fromResource("brand.svg").getLines().mkString
      assertEqualsFX(imageAsString, expected)
  }

  basicServerAndTestBody.testFX("string POST to /echo should echo back as a string") {
    case (serverAddressResource, body) =>
      serverAddressResource.use { serverAddress =>
        assertEqualsFX(
          basicRequest
            .post(Uri(new URI(s"${serverAddress}echo")))
            .body(body)
            .response(asString)
            .send[Http, ReceiveStreams](HttpScalaFXBackend())
            .httpValue
            .body
            .bind,
          body
        )
      }
  }

  basicServerAndTestBody.testFX("string POST to /echo should echo back as a stream") {
    case (serverAddressResource, body) =>
      serverAddressResource.use { serverAddress =>
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
  }

  basicServerAndTestBodyAndFile.testFX("string POST to /echo should echo back as a file") {
    case (serverAddressResource, body, fileResource) =>
      val fileContents = serverAddressResource.use { serverAddress =>
        fileResource.use { file =>
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
      }
      assertEqualsFX(fileContents, body)
  }

  basicServerAndTestBody.testFX("string POST to /echo should echo back as a byteArray") {
    case (serverAddressResource, body) =>
      assertEqualsFX(
        serverAddressResource.use { serverAddress =>
          new String(
            basicRequest
              .post(Uri(new URI(s"${serverAddress}echo")))
              .body(body)
              .response(asByteArray)
              .send[Http, ReceiveStreams](HttpScalaFXBackend())
              .httpValue
              .body
              .bind)
        },
        body
      )
  }

  basicServerAndTestBody.testFX(
    "string POST to /echo should echo back as a both byteArray and string") {
    case (serverAddressResource, body) =>
      val (Right(byteArray), Right(str)) = serverAddressResource.use { serverAddress =>
        basicRequest
          .post(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asBoth(asByteArray, asString))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
      }
      assertEqualsFX((new String(byteArray), str), ("test", "test"))
  }

  basicServerAndTestBody.testFX("string PUT to /echo should echo back as a string") {
    case (serverAddressResource, body) =>
      serverAddressResource.use { serverAddress =>
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
  }

  basicServerAndTestBody.testFX("string PUT to /echo should echo back as a stream") {
    case (serverAddressResource, body) =>
      serverAddressResource.use { serverAddress =>
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
  }

  basicServerAndTestBodyAndPutFile.testFX("string PUT to /echo should echo back as a file") {
    case (serverAddressResource, body, fileResource) =>
      val fileContents = serverAddressResource.use { serverAddress =>
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
      }
      assertEqualsFX(fileContents, body)
  }

  basicServerAndTestBody.testFX("string PUT to /echo should echo back as a byteArray") {
    case (serverAddressResource, body) =>
      assertEqualsFX(
        serverAddressResource.use { serverAddress =>
          new String(
            basicRequest
              .put(Uri(new URI(s"${serverAddress}echo")))
              .body(body)
              .response(asByteArray)
              .send[Http, ReceiveStreams](HttpScalaFXBackend())
              .httpValue
              .body
              .bind)
        },
        body
      )
  }

  basicServerAndTestBody.testFX(
    "string PUT to /echo should echo back as a both byteArray and string") {
    case (serverAddressResource, body) =>
      val (Right(byteArray), Right(str)) = serverAddressResource.use { serverAddress =>
        basicRequest
          .put(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asBoth(asByteArray, asString))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
      }
      assertEqualsFX((new String(byteArray), str), ("test", "test"))
  }

  basicServerAndTestBody.testFX("string PATCH to /echo should echo back as a string") {
    case (serverAddressResource, body) =>
      serverAddressResource.use { serverAddress =>
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
  }

  basicServerAndTestBody.testFX("string PATCH to /echo should echo back as a stream") {
    case (serverAddressResource, body) =>
      serverAddressResource.use { serverAddress =>
        assertEqualsFX(
          basicRequest
            .patch(Uri(new URI(s"${serverAddress}echo")))
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
  }

  basicServerAndTestBodyAndPatchFile.testFX(
    "string PATCH to /echo should echo back as a file") {
    case (serverAddressResource, body, fileResource) =>
      val fileContents = serverAddressResource.use { serverAddress =>
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
      }
      assertEqualsFX(fileContents, body)
  }

  basicServerAndTestBody.testFX("string PATCH to /echo should echo back as a byteArray") {
    case (serverAddressResource, body) =>
      assertEqualsFX(
        serverAddressResource.use { serverAddress =>
          new String(
            basicRequest
              .patch(Uri(new URI(s"${serverAddress}echo")))
              .body(body)
              .response(asByteArray)
              .send[Http, ReceiveStreams](HttpScalaFXBackend())
              .httpValue
              .body
              .bind)
        },
        body
      )
  }

  basicServerAndTestBody.testFX(
    "string PATCH to /echo should echo back as a both byteArray and string") {
    case (serverAddressResource, body) =>
      val (Right(byteArray), Right(str)) = serverAddressResource.use { serverAddress =>
        basicRequest
          .patch(Uri(new URI(s"${serverAddress}echo")))
          .body(body)
          .response(asBoth(asByteArray, asString))
          .send[Http, ReceiveStreams](HttpScalaFXBackend())
          .httpValue
          .body
      }
      assertEqualsFX((new String(byteArray), str), ("test", "test"))
  }
}
