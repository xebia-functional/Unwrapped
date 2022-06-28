package fx

import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.io.InputStream
import java.nio.file.Path

trait HttpBodyMapper[B]:
  def bodyPublisher(b: B): BodyPublisher

object HttpBodyMapper extends HttpBodyMapperLowPriority:

  given HttpBodyMapper[String] with
    def bodyPublisher(s: String): BodyPublisher =
      BodyPublishers.ofString(s)

  given HttpBodyMapper[Array[Byte]] with
    def bodyPublisher(b: Array[Byte]): BodyPublisher =
      BodyPublishers.ofByteArray(b)

  given HttpBodyMapper[Path] with
    def bodyPublisher(p: Path): BodyPublisher =
      BodyPublishers.ofFile(p)

  given HttpBodyMapper[InputStream] with
    def bodyPublisher(i: InputStream): BodyPublisher =
      BodyPublishers.ofInputStream(() => i)

trait HttpBodyMapperLowPriority:
  given HttpBodyMapper[Any] with
    def bodyPublisher(a: Any): BodyPublisher =
      BodyPublishers.noBody()

