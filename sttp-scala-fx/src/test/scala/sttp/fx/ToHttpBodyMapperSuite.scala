package sttp
package fx

import _root_.fx.{given, *}
import sttp.capabilities.Effect
import munit.fx.ScalaFXSuite

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.Flow
import sttp.client3.MultipartBody
import sttp.model.Part

class ToHttpBodyMapperSuite extends ScalaFXSuite, ToHttpBodyMapperFixtures {

  noBody.testFX(
    s"NoBody.type#toHttpBodyMapper() should return an HttpBodyMapper with ${MediaTypes.application.`octet-stream`.value}") {
    noBody =>
      val httpBodyMapper = noBody.toHttpBodyMapper()
      assertEqualsFX(httpBodyMapper.mediaType, MediaTypes.application.`octet-stream`)
  }

  noBody.testFX(
    "NoBody.type#toHttpBodyMapper() should return an HttpBodyMapper that publishes an empty body") {
    noBody =>
      val httpBodyMapper = noBody.toHttpBodyMapper()
      assertFX(streamed[Byte] {
        httpBodyMapper
          .bodyPublisher(noBody)
          .subscribe(new Flow.Subscriber[ByteBuffer]() {

            override def onError(x$0: Throwable): Unit = ()

            override def onComplete(): Unit = ()

            override def onSubscribe(subscription: Flow.Subscription): Unit = {
              subscription.request(Long.MaxValue)
            }

            override def onNext(x: ByteBuffer): Unit = x.reset.array.foreach(send)
          })
      }.toList.isEmpty)
  }

  byteArrayBody.testFX(
    s"ByteArrayBody#toHttpBodyMapper() should return an HttpBodyMapper with ${MediaTypes.application.`octet-stream`.value}") {
    byteArrayBody =>
      val httpBodyMapper = byteArrayBody.toHttpBodyMapper()
      assertEqualsFX(httpBodyMapper.mediaType, MediaTypes.application.`octet-stream`)
  }

  byteArrayBody.testFX(
    "ByteArrayBody#toHttpBodyMapper() should return an HttpBodyMapper that publishes the body's bytes as a Receive[Byte]") {
    byteArrayBody =>
      val httpBodyMapper = byteArrayBody.toHttpBodyMapper()
      assertEqualsFX(
        new String(
          streamed[Byte] {
            httpBodyMapper
              .bodyPublisher(byteArrayBody)
              .subscribe(new Flow.Subscriber[ByteBuffer]() {

                override def onError(x$0: Throwable): Unit = ()

                override def onComplete(): Unit = ()

                override def onSubscribe(subscription: Flow.Subscription): Unit =
                  subscription.request(Long.MaxValue)

                override def onNext(x: ByteBuffer): Unit = {
                  x.array.foreach { byte => send(byte) }
                }
              })
          }.toList.toArray,
          StandardCharsets.UTF_8),
        "test"
      )
  }

  byteBufferBody.testFX(
    s"ByteBufferBody#toHttpBodyMapper should return an HttpBodyMapper with ${MediaTypes.application.`octet-stream`.value}") {
    byteBufferBody =>
      val httpBodyMapper = byteBufferBody.toHttpBodyMapper()
      assertEqualsFX(httpBodyMapper.mediaType, MediaTypes.application.`octet-stream`)
  }

  byteBufferBody.testFX(
    "ByteBufferBody#toHttpBodyMapper() should return an HttpBodyMapper that publishes the body's bytes as a Receive[Byte]") {
    byteBufferBody =>
      val httpBodyMapper = byteBufferBody.toHttpBodyMapper()
      assertEqualsFX(
        new String(
          streamed[Byte] {
            httpBodyMapper
              .bodyPublisher(byteBufferBody)
              .subscribe(new Flow.Subscriber[ByteBuffer]() {

                override def onError(x$0: Throwable): Unit = ()

                override def onComplete(): Unit = ()

                override def onSubscribe(subscription: Flow.Subscription): Unit =
                  subscription.request(Long.MaxValue)

                override def onNext(x: ByteBuffer): Unit = {
                  x.array.foreach { byte => send(byte) }
                }
              })
          }.toList.toArray,
          StandardCharsets.UTF_8),
        "test"
      )
  }

  inputStreamBody.testFX(
    s"InputStreamBody#toHttpBodyMapper() should return an HttpBodyMapper with ${MediaTypes.application.`octet-stream`.value}") {
    inputStreamBody =>
      val httpBodyMapper = inputStreamBody.toHttpBodyMapper()
      assertEqualsFX(httpBodyMapper.mediaType, MediaTypes.application.`octet-stream`)
  }

  inputStreamBody.testFX(
    "InputStreamBody#toHttpBodyMapper() should return an HttpBodyMapper that publishes the body's bytes as a Receive[Byte]") {
    inputStreamBody =>
      val httpBodyMapper = inputStreamBody.toHttpBodyMapper()
      assertEqualsFX(
        streamed[String] {
          httpBodyMapper
            .bodyPublisher(inputStreamBody)
            .subscribe(new Flow.Subscriber[ByteBuffer]() {

              override def onError(x$0: Throwable): Unit = ()

              override def onComplete(): Unit = ()

              override def onSubscribe(subscription: Flow.Subscription): Unit =
                subscription.request(Long.MaxValue)

              override def onNext(x: ByteBuffer): Unit = {
                send(StandardCharsets.UTF_8.decode(x).toString())
              }
            })
        }.toList.mkString,
        "test"
      )
  }

  fileBody.testFX(
    s"FileBody#toHttpBodyMapper() should return an HttpBodyMapper with ${MediaTypes.application.`octet-stream`.value}") {
    fileBodyResource =>
      val mediaType = fileBodyResource.use { fileBody => fileBody.toHttpBodyMapper().mediaType }
      assertEqualsFX(mediaType, MediaTypes.application.`octet-stream`)
  }

  fileBody.testFX(
    "FileBody#toHttpBodyMapper() should return an HttpBodyMapper that publishes the body's bytes as a Receive[Byte]") {
    fileBodyResource =>
      val actualBody = fileBodyResource.use { fileBody =>
        val httpBodyMapper = fileBody.toHttpBodyMapper()
        streamed[String] {
          httpBodyMapper
            .bodyPublisher(fileBody)
            .subscribe(new Flow.Subscriber[ByteBuffer]() {

              override def onError(x$0: Throwable): Unit = ()

              override def onComplete(): Unit = ()

              override def onSubscribe(subscription: Flow.Subscription): Unit =
                subscription.request(Long.MaxValue)

              override def onNext(x: ByteBuffer): Unit = {
                send(StandardCharsets.UTF_8.decode(x).toString())
              }
            })
        }.toList.mkString
      }
      assertEqualsFX(actualBody, "test")
  }

  streamBody.testFX(
    s"StreamBody[fx.Receive[Byte], sttp.fx.ReceiveStreams]#toHttpBodyMapper should return an HttpBodyMapper with ${MediaTypes.application.`octet-stream`}") {
    streamBody =>
      assertEqualsFX(
        streamBody.toHttpBodyMapper().mediaType,
        MediaTypes.application.`octet-stream`)
  }

  streamBody.testFX(
    s"StreamBody[Receive[Byte], sttp.fx.ReceiveStreams]#toHttpBodyMapper should return an HttpBodyMapper that publishes the body as a stream of bytes") {
    streamBody =>
      val httpBodyMapper = streamBody.toHttpBodyMapper()
      assertEqualsFX(
        streamed[String] {
          httpBodyMapper
            .bodyPublisher(streamBody)
            .subscribe(new Flow.Subscriber[ByteBuffer]() {

              override def onError(x$0: Throwable): Unit = ()

              override def onComplete(): Unit = ()

              override def onSubscribe(subscription: Flow.Subscription): Unit =
                subscription.request(Long.MaxValue)

              override def onNext(x: ByteBuffer): Unit = {
                send(StandardCharsets.UTF_8.decode(x).toString())
              }
            })
        }.toList.mkString,
        "test"
      )
  }

  multiparts.testFX(
    s"MultipartBody[ReceiveStreams with Effect[Http]]#toHttpBodyMapper should return an HttpBodyMapper that has a multipart media type") {
    case (
          noBody,
          byteArrayBody,
          (byteBufferBody, inputStreamBody, (fileBodyResource, streamBody))) =>
      fileBodyResource.use { fileBody =>
        assertFX(
          MultipartBody(Seq(
            Part("noboy", noBody),
            Part("byteArrayBody", byteArrayBody),
            Part("byteBufferBody", byteBufferBody),
            Part("inputStreamBody", inputStreamBody),
            Part("fileBody", fileBody),
            Part("streamBody", streamBody)
          )).toHttpBodyMapper().mediaType.value.contains("multipart/form-data; boundary="))
      }

  }

  multiparts.testFX(
    s"MultipartBody[ReceiveStreams with Effect[Http]]#toHttpBodyMapper should encode the body as a stream of bytes") {
    case (
          noBody,
          byteArrayBody,
          (byteBufferBody, inputStreamBody, (fileBodyResource, streamBody))) =>
      fileBodyResource.use { fileBody =>
        val multipartBody = MultipartBody(
          Seq(
            Part("noboy", noBody),
            Part("byteArrayBody", byteArrayBody),
            Part("byteBufferBody", byteBufferBody),
            Part("inputStreamBody", inputStreamBody),
            Part("fileBody", fileBody),
            Part("streamBody", streamBody)
          ))
        val httpBodyMapper: HttpBodyMapper[MultipartBody[ReceiveStreams]] =
          multipartBody.toHttpBodyMapper()
        val expectedNobodyPart =
          """Content-Disposition: form-data; name="noboy; filename=noboy"""
        val expectedByteArrayBodyPart =
          """Content-Disposition: form-data; name="noboy; filename=noboy"""
        val expectedByteBufferBody =
          """Content-Disposition: form-data; name="byteBufferBody; filename=byteBufferBody"""
        val expectedInputStreamBody =
          """Content-Disposition: form-data; name="inputStreamBody; filename=inputStreamBody"""
        val expectedFileBodyPartHeader = """Content-Disposition: form-data; name=fileBody; """
        val expectedFileBodyPortBody = """Content-Type: text/plain"""
        val expectedStreamBodyPart =
          """Content-Disposition: form-data; name="streamBody; filename=streamBody"""
        val obtained = streamed[String] {
          httpBodyMapper
            .bodyPublisher(multipartBody)
            .subscribe(new Flow.Subscriber[ByteBuffer]() {

              override def onError(x$0: Throwable): Unit = ()

              override def onComplete(): Unit = ()

              override def onSubscribe(subscription: Flow.Subscription): Unit =
                subscription.request(Long.MaxValue)

              override def onNext(x: ByteBuffer): Unit = {
                send(StandardCharsets.UTF_8.decode(x).toString())
              }
            })
        }.toList.mkString
        val expectation = obtained.contains(expectedNobodyPart) && obtained.contains(
          expectedByteArrayBodyPart) && obtained.contains(expectedByteBufferBody) && obtained
          .contains(expectedInputStreamBody) && obtained.contains(
          expectedFileBodyPartHeader) && obtained.contains(expectedFileBodyPortBody) && obtained
          .contains(expectedStreamBodyPart) && obtained.contains("test")
        assertFX(
          expectation
        )
      }
  }

  stringBody.testFX(
    s"StringBody#toHttpBodyMapper should have a media type containing ${MediaTypes.text.plain}") {
    stringBody => assertEqualsFX(stringBody.toHttpBodyMapper().mediaType.value, "text/plain")
  }

  stringBody.testFX(
    s"StringBody#toHttpBodyMapper should return an HttpBodyMapper that publishes the body as a stream of bytes") {
    stringBody =>
      val httpBodyMapper = stringBody.toHttpBodyMapper()
      assertEqualsFX(
        streamed[String] {
          httpBodyMapper
            .bodyPublisher(stringBody)
            .subscribe(new Flow.Subscriber[ByteBuffer]() {

              override def onError(x$0: Throwable): Unit = ()

              override def onComplete(): Unit = ()

              override def onSubscribe(subscription: Flow.Subscription): Unit =
                subscription.request(Long.MaxValue)

              override def onNext(x: ByteBuffer): Unit = {
                send(StandardCharsets.UTF_8.decode(x).toString())
              }
            })
        }.toList.mkString,
        "test"
      )
  }

}
