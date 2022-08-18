package sttp
package fx

import _root_.fx.{*, given}
import munit.fx.ScalaFXSuite

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.Flow

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
                  x.array.foreach { byte =>
                    send(byte)
                  }
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
                  x.array.foreach { byte =>
                    send(byte)
                  }
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
}
