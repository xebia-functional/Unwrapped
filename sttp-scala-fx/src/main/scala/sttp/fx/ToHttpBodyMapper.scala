package sttp.fx

import _root_.fx.*
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
import java.net.http.Boundary
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.MultiPartBodyPublisher
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom
import scala.util.Random
import scala.util.Try
import sttp.capabilities.Effect

trait ToHttpBodyMapper[-A]:
  /**
   * Maps an sttp request body to its appropriate HttpBodyMapper. Note the bodyPublishers in the returned HttpBodyMapper
   * this operation often requires the use of side effecting code and
   * may throw. It can only be safely executed within an appropriate
   * handling effect, which is where ALL of the current implementation calls the bodyPublisher methods within this 
   */
  extension (a: A) def toHttpBodyMapper(): HttpBodyMapper[A]

object ToHttpBodyMapper:
  /**
   * Summons the approprite mapper by request body type from implicit scope
   */
  def apply[A](): ToHttpBodyMapper[A] ?=> ToHttpBodyMapper[A] =
    summon

  /** So we have to inline all the body mappers below so that we can get it widened to R
    *
    */
  def apply[R >: ReceiveStreams with Effect[Http]](body: RequestBody[R]): ToHttpBodyMapper[RequestBody[R]] =
    ???


  /**
   * Tries to encode the body using its stated charset. If the charset is unsupported, encodes
   * the body as a byte array for sending with the default charset.
   */
  given stringBodyMapper(using HttpBodyMapper[Array[Byte]]): ToHttpBodyMapper[StringBody] =
    new ToHttpBodyMapper[StringBody]:
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

  given byteBufferBodyToHttpBodyMapper(
      using HttpBodyMapper[Array[Byte]]): ToHttpBodyMapper[ByteBufferBody] =
    new ToHttpBodyMapper[ByteBufferBody]:
      extension (a: ByteBufferBody)
        def toHttpBodyMapper(): HttpBodyMapper[ByteBufferBody] =
          new HttpBodyMapper[ByteBufferBody]:
            override def mediaType: MediaType =
              MediaType(a.defaultContentType.toString)
            override def bodyPublisher(b: ByteBufferBody): BodyPublisher =
              summon[HttpBodyMapper[Array[Byte]]].bodyPublisher(b.b.rewind().array())

  given inputStreamToHttpBodyMapper(
      using HttpBodyMapper[InputStream]): ToHttpBodyMapper[InputStreamBody] =
    new ToHttpBodyMapper[InputStreamBody]:
      extension (a: InputStreamBody)
        def toHttpBodyMapper() =
          new HttpBodyMapper[InputStreamBody]:
            override def bodyPublisher(b: InputStreamBody): BodyPublisher =
              summon[HttpBodyMapper[InputStream]].bodyPublisher(b.b)
            override def mediaType: MediaType =
              MediaType(a.defaultContentType.toString)

  given fileBodyToHttpBodyMapper(using HttpBodyMapper[Path]): ToHttpBodyMapper[FileBody] =
    new ToHttpBodyMapper[FileBody]:
      extension (a: FileBody)
        def toHttpBodyMapper() =
          new HttpBodyMapper[FileBody]:
            override def bodyPublisher(b: FileBody): BodyPublisher =
              summon[HttpBodyMapper[Path]].bodyPublisher(b.f.toPath)
            override def mediaType: MediaType =
              MediaType(a.defaultContentType.toString)

  given streamBodyMapper(
      using HttpBodyMapper[Receive[Byte]]
  ): ToHttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]] =
    new ToHttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]]:
      extension (a: StreamBody[Receive[Byte], ReceiveStreams])
        def toHttpBodyMapper(): HttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]] =
          new HttpBodyMapper[StreamBody[Receive[Byte], ReceiveStreams]]:
            override def mediaType: MediaType = MediaType(a.defaultContentType.toString)
            override def bodyPublisher(
                b: StreamBody[Receive[Byte], ReceiveStreams]
            ): BodyPublisher =
              summon[HttpBodyMapper[Receive[Byte]]].bodyPublisher(a.b)

  /**
   * Maps multipart request bodies to a java http body pubilisher. Limitations of the StreamBody
   * dectlaration force the casts in this publisher.
   */
  given multipartBody[R]: ToHttpBodyMapper[MultipartBody[R]] =
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
                }.fold(_ => MediaType.apply(a.defaultContentType.toString), MediaType.apply)
                  .value
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
