package fx

import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.OpenOption
import java.net.http.HttpHeaders
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.ResponseInfo
import java.util.concurrent.Flow
import java.util.concurrent.CompletionStage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import scala.jdk.FunctionConverters.*
import scala.jdk.CollectionConverters.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.nio.ByteBuffer

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

  given HttpResponseReceiveMapper(using s: Structured): HttpResponseMapper[fx.Receive[Byte]] =
    new HttpResponseMapper[fx.Receive[Byte]] {

      def bodyHandler: BodyHandler[fx.Receive[Byte]] = new BodyHandler[fx.Receive[Byte]] {
          def apply(responseInfo: HttpResponse.ResponseInfo)
              : HttpResponse.BodySubscriber[fx.Receive[Byte]] =
            new HttpResponse.BodySubscriber[fx.Receive[Byte]] {
              val queue = new ConcurrentLinkedQueue[Byte] {}
              val err: AtomicReference[Throwable] = new AtomicReference()
              val executor = Executors.newVirtualThreadPerTaskExecutor
              val isDone = AtomicBoolean(false)
              def getBody(): CompletionStage[fx.Receive[Byte]] =
                CompletableFuture.supplyAsync(
                  () =>
                    streamed {
                      def loop(b: Boolean): Unit = {
                        if (b)
                          val ex = err.get
                          if (ex != null)
                            isDone.set(true)
                            throw ex
                          else
                            Nullable(queue.poll).map(send).getOrElse(())
                            Thread.`yield`
                            loop(!isDone.get())
                        else ()
                      }
                      loop(!isDone.get())
                    },
                  executor
                )

              private def shutdown =
                isDone.set(true)
                try
                  if (!executor.isShutdown())
                    executor.awaitTermination(10, TimeUnit.SECONDS)
                    executor.shutdown
                  else ()
                catch case _ => ()

              def onComplete(): Unit = shutdown
              def onError(ex: Throwable): Unit = err.updateAndGet(t => ex)
              def onNext(byteBuffers: java.util.List[java.nio.ByteBuffer]): Unit = for {
                bb: ByteBuffer <- byteBuffers.asScala
                b <- bb.array
              } queue.add(b)
              def onSubscribe(x$0: Flow.Subscription): Unit =
                isDone.set(false)
                err.updateAndGet(_ => null)
            }
        }
    }

  given fileDownloadHttpResponseMapper(using Path, Seq[OpenOption]): HttpResponseMapper[Path] =
    new HttpResponseMapper[Path] {
      def bodyHandler = BodyHandlers.ofFile(summon[Path], summon[Seq[OpenOption]]: _*)
    }
