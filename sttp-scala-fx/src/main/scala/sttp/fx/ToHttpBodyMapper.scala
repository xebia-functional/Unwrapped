package sttp.fx

import _root_.fx.*
import sttp.capabilities.Effect
import sttp.capabilities.Streams
import sttp.client3.ByteArrayBody
import sttp.client3.ByteBufferBody
import sttp.client3.FileBody
import sttp.client3.InputStreamBody
import sttp.client3.MultipartBody
import sttp.client3.NoBody
import sttp.client3.RequestBody
import sttp.client3.StreamBody
import sttp.client3.StringBody
import sttp.model.Part

import java.io.ByteArrayInputStream
import java.io.InputStream
import fx.Boundary
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import fx.MultiPartBodyPublisher
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom
import scala.util.Random
import scala.util.Try

trait ToHttpBodyMapper[A]:
  /**
   * Maps an sttp request body to its appropriate HttpBodyMapper. Note the bodyPublishers in the
   * returned HttpBodyMapper this operation often requires the use of side effecting code and
   * may throw. It can only be safely executed within an appropriate handling effect, which is
   * where ALL of the current implementation calls the bodyPublisher methods within this
   */
  extension (a: A) def toHttpBodyMapper(): HttpBodyMapper[A]

given ToHttpBodyMapper[NoBody.type] with
  extension (a: NoBody.type)
    def toHttpBodyMapper(): HttpBodyMapper[NoBody.type] =
      new HttpBodyMapper[NoBody.type] {
        def mediaType: MediaType = MediaTypes.application.`octet-stream`
        def bodyPublisher(b: NoBody.type): BodyPublisher =
          BodyPublishers.noBody()
      }

given ToHttpBodyMapper[ByteArrayBody] with
  extension (a: ByteArrayBody)
    def toHttpBodyMapper(): HttpBodyMapper[ByteArrayBody] =
      new HttpBodyMapper[ByteArrayBody]:
        override def mediaType: MediaType = MediaTypes.application.`octet-stream`
        override def bodyPublisher(b: ByteArrayBody): BodyPublisher =
          summon[HttpBodyMapper[Array[Byte]]].bodyPublisher(b.b)

given ToHttpBodyMapper[ByteBufferBody] with
  extension (a: ByteBufferBody)
    def toHttpBodyMapper(): HttpBodyMapper[ByteBufferBody] =
      new HttpBodyMapper[ByteBufferBody]:
        override def mediaType: MediaType =
          MediaType(a.defaultContentType.toString)
        override def bodyPublisher(b: ByteBufferBody): BodyPublisher =
          summon[HttpBodyMapper[Array[Byte]]].bodyPublisher(b.b.rewind().array())

given ToHttpBodyMapper[InputStreamBody] with
  extension (a: InputStreamBody)
    def toHttpBodyMapper() =
      new HttpBodyMapper[InputStreamBody]:
        override def bodyPublisher(b: InputStreamBody): BodyPublisher =
          summon[HttpBodyMapper[InputStream]].bodyPublisher(b.b)
        override def mediaType: MediaType =
          MediaType(a.defaultContentType.toString)

given ToHttpBodyMapper[FileBody] with
  extension (a: FileBody)
    def toHttpBodyMapper() =
      new HttpBodyMapper[FileBody]:
        override def bodyPublisher(b: FileBody): BodyPublisher =
          summon[HttpBodyMapper[Path]].bodyPublisher(b.f.toPath)
        override def mediaType: MediaType =
          MediaType(a.defaultContentType.toString)

given ToHttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]] with
  extension (a: StreamBody[Receive[Byte], ReceiveStreams])
    def toHttpBodyMapper(): HttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]] =
      new HttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]]:
        override def mediaType: MediaType = MediaType(a.defaultContentType.toString)
        override def bodyPublisher(
            b: StreamBody[Receive[Byte], ReceiveStreams]
        ): BodyPublisher =
          summon[HttpBodyMapper[Receive[Byte]]].bodyPublisher(a.b)

given multipartBodyMapper[R]: ToHttpBodyMapper[MultipartBody[R]] =
  new ToHttpBodyMapper[MultipartBody[R]]:
    extension (a: MultipartBody[R])
      def toHttpBodyMapper(): HttpBodyMapper[MultipartBody[R]] =
        new HttpBodyMapper[MultipartBody[R]]:
          private val boundary = Boundary(new Random(new SecureRandom()).nextLong.toHexString)
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: StringBody): MultiPartBodyPublisher =
            publisher.addPart(name, body.s)
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: ByteArrayBody): MultiPartBodyPublisher =
            publisher.addPart(
              name,
              () => new ByteArrayInputStream(body.b),
              body.defaultContentType.toString)
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: ByteBufferBody): MultiPartBodyPublisher =
            publisher.addPart(
              name,
              () => new ByteArrayInputStream(body.b.rewind().array()),
              body.defaultContentType.toString)
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: InputStreamBody): MultiPartBodyPublisher =
            publisher.addPart(name, () => body.b, body.defaultContentType.toString)
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: FileBody): MultiPartBodyPublisher =
            val file = body.f
            publisher.addPart(
              name,
              () => new ByteArrayInputStream(file.readAsByteArray),
              body.f.toFile.getName(),
              body.f.toPath,
              Try {
                Option(Files.probeContentType(file.toPath)).get
              }.fold(_ => MediaType.apply(a.defaultContentType.toString), MediaType.apply).value
            )
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: Receive[Byte]): MultiPartBodyPublisher =
            publisher.addPart(
              name,
              () => new ByteArrayInputStream(body.toList.toArray),
              MediaTypes.application.`octet-stream`.value
            )
          private def handleBody(
              publisher: MultiPartBodyPublisher,
              name: String,
              body: NoBody.type): MultiPartBodyPublisher =
            publisher.addPart(
              name,
              () => new ByteArrayInputStream(Array.emptyByteArray),
              MediaTypes.application.`octet-stream`.value
            )
          override def mediaType: MediaType = MediaType(
            s"${a.defaultContentType.toString}; boundary=${boundary.value}")
          override def bodyPublisher(b: MultipartBody[R]): BodyPublisher =
            b.parts
              .foldLeft(MultiPartBodyPublisher(boundary)) {
                case (currentPublisher, Part(name, body @ StringBody(_, _, _), _, _)) =>
                  handleBody(currentPublisher, name, body)
                case (currentPublisher, Part(name, body @ ByteArrayBody(_, _), _, _)) =>
                  handleBody(currentPublisher, name, body)
                case (currentPublisher, Part(name, body @ ByteBufferBody(_, _), _, _)) =>
                  handleBody(currentPublisher, name, body)
                case (currentPublisher, Part(name, body @ InputStreamBody(_, _), _, _)) =>
                  handleBody(currentPublisher, name, body)
                case (currentPublisher, Part(name, body @ FileBody(_, _), _, _)) =>
                  handleBody(currentPublisher, name, body)
                case (currentPublisher, Part(name, body @ StreamBody(_), _, _))
                    if Try(body.asInstanceOf[StreamBody[Receive[Byte], ReceiveStreams]])
                      .toOption
                      .isDefined =>
                  handleBody(
                    currentPublisher,
                    name,
                    body.asInstanceOf[StreamBody[Receive[Byte], ReceiveStreams]].b)
                case (currentPublisher, Part(name, NoBody, _, _)) =>
                  handleBody(currentPublisher, name, NoBody)
                case _ => throw new Exception("Nested multipart bodies are unsupported")
              }
              .unsafeTobodyPublisher()

given ToHttpBodyMapper[StringBody] with
  extension (a: StringBody)
    def toHttpBodyMapper(): HttpBodyMapper[StringBody] =
      Try(Charset.forName(a.encoding)).fold(
        _ => {
          new HttpBodyMapper[StringBody]:
            override def mediaType: MediaType =
              MediaTypes.application.`octet-stream`
            override def bodyPublisher(s: StringBody): BodyPublisher =
              summon[HttpBodyMapper[Array[Byte]]].bodyPublisher(s.s.getBytes)
        },
        charset => {
          new HttpBodyMapper[StringBody]:
            override def mediaType: MediaType =
              MediaType(a.defaultContentType.toString)
            override def bodyPublisher(b: StringBody): BodyPublisher =
              BodyPublishers.ofString(b.s, charset)
        }
      )

object ToHttpBodyMapper:
  /**
   * Summons the approprite mapper by request body type from implicit scope
   */
  def apply[A]()(using ToHttpBodyMapper[A]): ToHttpBodyMapper[A] =
    summon
