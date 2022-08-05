package sttp
package fx

import _root_.fx.*
import sttp.client3.ByteArrayBody
import sttp.client3.ByteBufferBody
import sttp.client3.FileBody
import sttp.client3.InputStreamBody
import sttp.client3.StreamBody
import sttp.client3.StringBody

import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.nio.charset.Charset
import java.nio.file.Files
import scala.util.Try
import sttp.client3.RequestBody
import sttp.client3.MultipartBody

trait ToHttpBodyMapper[A <: RequestBody[_]]:
  extension (a: A) def toHttpBodyMapper(): HttpBodyMapper[A]

object ToHttpBodyMapper:
  /**
   * Tries to encode the body using its stated charset. If the charset is unsupported, encodes
   * the body as a byte array for sending with the default charset.
   */
  given ToHttpBodyMapper[StringBody] with
    extension (a: StringBody)
      def toHttpBodyMapper(): HttpBodyMapper[StringBody] =
        Try(Charset.forName(a.encoding)).fold(
          _ => {
            new HttpBodyMapper[StringBody]:
              override def mediaType: MediaType =
                MediaTypes.application.`octet-stream`
              override def bodyPublisher(s: StringBody): BodyPublisher =
                BodyPublishers.ofByteArray(s.s.getBytes)
          },
          charset => {
            new HttpBodyMapper[StringBody]:
              override def mediaType: MediaType =
                MediaType(s"text/plain; charset=${charset.name()}")
              override def bodyPublisher(b: StringBody): BodyPublisher =
                BodyPublishers.ofString(b.s, charset)
          }
        )

  given ToHttpBodyMapper[ByteBufferBody] with
    extension (a: ByteBufferBody)
      def toHttpBodyMapper(): HttpBodyMapper[ByteBufferBody] =
        new HttpBodyMapper[ByteBufferBody]:
          override def bodyPublisher(b: ByteArrayBody): BodyPublisher =
            BodyPublishers.ofByteArray(b.b)

  given ToHttpBodyMapper[InputStreamBody] with
    extension (a: InputStreamBody)
      def toHttpBodyMapper() =
        new HttpBodyMapper[InputStreamBody]:
          override def bodyPublisher(b: InputStreamBody): BodyPublisher =
            BodyPublishers.ofInputStream(() => b.b)
          override def mediaType: MediaType =
            MediaTypes.application.`octet-stream`

  given ToHttpBodyMapper[FileBody] = new ToHttpBodyMapper[FileBody]:
    extension (a: FileBody)
      def toHttpBodyMapper() =
        new HttpBodyMapper[FileBody]:
          override def bodyPublisher(b: FileBody): BodyPublisher =
            BodyPublishers.ofFile(b.f.toPath)
          override def mediaType: MediaType =
            Try {
              Option(Files.probeContentType(a.f.toPath)).get
            }.fold(_ => MediaType.apply(a.defaultContentType.toString), MediaType.apply)

  given streamBodyMapper(
      using
      HttpBodyMapper[Receive[Byte]]): HttpBodyMapper[StreamBody[Receive[Byte], Receive[Byte]]] =
    new HttpBodyMapper[StreamBody[Receive[Byte], Receive[Byte]]]:
      extension (a: StreamBody[Receive[Byte], Receive[Byte]])
        def toHttpBodyMapper: HttpBodyMapper[StreamBody[Receive[Byte], Receive[Byte]]] =
          new HttpBodyMapper[StreamBody[Receive[Byte], Receive[Byte]]]:
            override def mediaType: MediaType = MediaTypes.application.`octet-stream`
            override def bodyPublisher(
                b: StreamBody[Receive[Byte], Receive[Byte]]): BodyPublisher =
              summon[HttpBodyMapper[Receive[Byte]]].bodyPublisher(a.b)

  // given ToHttpBodyMapper[MultipartBody[_]] with
  //   extension(a: MultipartBody[_])
  //     def toHttpBodyMapper: HttpBodyMapper[MultipartBody[_]] =
  //       new HttpBodyMapper[MultipartBody[_]]:
  //         private val boundary = java.lang.Long.toHexString(System.currentTimeMillis())
  //         override def mediaType: MediaType = MediaType(s"${MediaTypes.multipart.`form-data`.value}; boundary=$boundary")
  //         override def bodyPublisher(b: MultipartBody[_]): BodyPublisher =
  //           new MulitpartBodyPublisher()
          
