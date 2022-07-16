package fx

import java.io.InputStream
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.concurrent.SubmissionPublisher

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

  /** A streaming byte body publisher that sends bytes one at a time,
    * always re-sending when a byte is dropped by the receiving
    * subscriber
    *
    */
  given HttpBodyMapper[Receive[Byte]] with
    def bodyPublisher(b: Receive[Byte]): BodyPublisher =
      new BodyPublisher:
        def contentLength(): Long =
          -1L

        def subscribe(
            subscriber: java.util.concurrent.Flow.Subscriber[? >: java.nio.ByteBuffer]): Unit =
          val publisher = new SubmissionPublisher[java.nio.ByteBuffer] {}
          publisher.subscribe(subscriber)
          val x: Send[Byte] ?=> (Byte) => Unit = byte =>
            while(publisher.offer( //this coubl be rewritten as a tail
                                   //recursive function... but as the
                                   //compiler will reduce to this
                                   //while loop, what is the point?
              ByteBuffer.wrap(new Array[Byte](byte)),
              (subscriber, droppedByteBuffer) => true
            ) < 0){
              ()
            } // always resend dropped bytes
          b.transform(x)

trait HttpBodyMapperLowPriority:
  given HttpBodyMapper[Any] with
    def bodyPublisher(a: Any): BodyPublisher =
      BodyPublishers.noBody()
