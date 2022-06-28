package fx

import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.OpenOption
import java.net.http.HttpHeaders
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.ResponseInfo

trait HttpResponseMapper[A]:
  def bodyHandler: HttpResponse.BodyHandler[A]

object HttpResponseMapper:

  given HttpResponseMapper[Void] with
    def bodyHandler = BodyHandlers.discarding

  given HttpResponseMapper[String] with
    def bodyHandler = BodyHandlers.ofString()

  given HttpResponseMapper[Array[Byte]] with
    def bodyHandler = BodyHandlers.ofByteArray()

  given HttpResponseMapper[InputStream] with
    def bodyHandler = BodyHandlers.buffering(BodyHandlers.ofInputStream(), 4096)

  given fileDownloadHttpResponseMapper(using Path, Seq[OpenOption]): HttpResponseMapper[Path] =
    new HttpResponseMapper[Path] {
      def bodyHandler = BodyHandlers.ofFile(summon[Path], summon[Seq[OpenOption]]: _*)
    }
