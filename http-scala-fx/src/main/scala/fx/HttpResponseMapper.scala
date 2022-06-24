package fx

import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse.BodyHandler
import java.util.function.Consumer
import java.util.Optional
import java.io.InputStream
import java.nio.charset.Charset
import java.net.http.HttpResponse.BodySubscriber
import java.util.concurrent.CompletionStage

trait HttpResponseMapper[-A]:
  extension (h: Http[A]) def bodyHandler: BodyHandler[A]

object HttpMapper:

  class StringHttpResponseMapper[String] extends HttpResponseMapper[String]:
    extension (h: Http[String])
      def bodyHandler =
        BodyHandlers.ofString()

  class ByteArrayHttpResponseMapper[Array[Byte]] extends HttpResponseMapper[Array[Byte]]:
    extension (h: Http[Array[Byte]])
      def bodyHandler =
        BodyHandlers.ofByteArray()

  class InputStreamHttpResponseMapper[InputStream] extends HttpResponseMapper[InputStream]:
    extension (h: Http[InputStream])
      def bodyHandler =
        BodyHandlers.ofInputStream()

  class CharsetHttpResponseMapper[String](charset: Charset) extends HttpResponseMapper[String]:
    extension (h: Http[String])
      def bodyHandler =
        BodyHandlers.ofString(charset)


  class ReceiveHttpResponseMapper extends HttpResponseMapper[Receive[Byte]]:
    extension (h: Http[Receive[String]])
      def bodyHandler = ??? // todo figure out how to make this stream
        
