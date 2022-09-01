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
import java.io.StringWriter
import java.io.PrintWriter

trait HttpResponseMapper[A]:
  def bodyHandler: HttpResponse.BodyHandler[A]

object HttpResponseMapper:

  given HttpResponseMapper[T: Serde]: HttpResponseMapper[T] = new HttpResponseMapper[T] {
    def bodyHandler: BodyHandler[T] = new HttpResponse.BodyHandler[T] {
      def apply(responseInfo: HttpResponse.ResponseInfo) = new HttpResponse.BodySubscriber[T] {
        val accumulator = new AtomicReference(List[ByteBuffer]())
        val result = new CompletableFuture[T]()
        override def getBody(): CompletionStage[T] =
          result
        override def onComplete(): Unit =
          try { // getting the value can actually fail
            val x = Serde[T]().deserialize(accumulator.get())
            x match {
              case errors @ h :: t =>
                onError(
                  HttpExecutionException(
                    new RuntimeException(
                      s"Errors deserializing http response: ${errors.mkString("\n")}")))
              case _ => result.complete(x.asInstanceOf[T])
            }
          } catch {
            case ex: Throwable =>
              onError(HttpExecutionException(RuntimeException(ex.getMessage(), ex)))
          }
        override def onError(ex: Throwable): Unit =
          result.completeExceptionally(ex)
        override def onNext(byteBuffers: java.util.List[java.nio.ByteBuffer]): Unit =
          accumulator.updateAndGet(current => current ++ byteBuffers.asScala.toList)
          ()
        override def onSubscribe(subscription: Flow.Subscription): Unit =
          subscription.request(Long.MaxValue)
      }
    }
  }
  given HttpResponseMapper[Void] with
    def bodyHandler = BodyHandlers.discarding

  given HttpResponseMapper[String] with
    def bodyHandler = BodyHandlers.ofString()

  given HttpResponseMapper[Array[Byte]] with
    def bodyHandler = BodyHandlers.ofByteArray()

  given HttpResponseMapper[InputStream] with
    def bodyHandler = BodyHandlers.buffering(BodyHandlers.ofInputStream(), 4096)

  given HttpResponseReceiveMapper: HttpResponseMapper[fx.Receive[Byte]] =
    new HttpResponseMapper[fx.Receive[Byte]] {
      def bodyHandler: BodyHandler[fx.Receive[Byte]] = BodyHandlers.buffering(
        new BodyHandler[fx.Receive[Byte]] {
          def apply(responseInfo: HttpResponse.ResponseInfo)
              : HttpResponse.BodySubscriber[fx.Receive[Byte]] =
            new HttpResponse.BodySubscriber[fx.Receive[Byte]] {
              val debug = false
              def printDebugMessage(message: String): Unit =
                if (debug)
                  println(message)
                else
                  ()
              val queue = new ConcurrentLinkedQueue[Byte] {}
              val err: AtomicReference[Throwable] = new AtomicReference()
              val executor = Executors.newVirtualThreadPerTaskExecutor
              val shutdownExecutor = Executors.newScheduledThreadPool(1)
              val isDone = AtomicBoolean(false)
              def getBody(): CompletionStage[fx.Receive[Byte]] =
                CompletableFuture.supplyAsync(
                  () =>
                    streamed {
                      def loop(b: Boolean): Unit = {
                        printDebugMessage(s"getBody:loop: $b")
                        if (b)
                          val ex = err.get
                          if (ex != null)
                            printDebugMessage(s"getBody:loop:${ex.getMessage()}")
                            shutdown(true)
                            throw ex
                          else
                            Nullable(queue.poll)
                              .map { byte =>
                                printDebugMessage(s"getBody:loop:byte:${byte.toInt}")
                                send(byte)
                              }
                              .getOrElse(())
                            printDebugMessage(
                              s"getBody:loop:yielding on ${Thread.currentThread.getName}")
                            Thread.`yield`
                            printDebugMessage("getBody:loop:resuming loop")
                            loop(!isDone.get())
                        else ()
                      }
                      loop(!isDone.get())
                    },
                  executor
                )

              private def shutdown(now: Boolean): Unit =
                if (queue.size > 0 && !now) {
                  printDebugMessage(s"shutdown: items remain in queue to be sent, waiting...")
                  shutdownExecutor.schedule(
                    new Runnable {
                      override def run() = shutdown(false)
                    },
                    10,
                    TimeUnit.MILLISECONDS)
                } else {
                  printDebugMessage("shutdown:shutting down")
                  isDone.set(true)
                  try
                    if (!executor.isShutdown())
                      executor.awaitTermination(10, TimeUnit.SECONDS)
                      executor.shutdown
                      shutdownExecutor.shutdown
                    else ()
                  catch case _ => ()
                }

              def onComplete(): Unit =
                printDebugMessage("onComplete:complete")
                shutdown(false)
              def onError(ex: Throwable): Unit =
                val sw: StringWriter = new StringWriter()
                val pw: PrintWriter = new PrintWriter(sw)
                ex.printStackTrace(pw)
                printDebugMessage(s"onError: ${ex}, ${ex.getMessage()}, ${sw.toString()}")
                err.updateAndGet(t => ex)
              def onNext(byteBuffers: java.util.List[java.nio.ByteBuffer]): Unit =
                printDebugMessage(s"onNext: ${byteBuffers.size}")
                for {
                  bb: ByteBuffer <- byteBuffers.asScala.toList
                } while (bb.hasRemaining()) {
                  val b = bb.get()
                  printDebugMessage(s"onNext: enqueueing current byte: ${b.toInt.toHexString}")
                  queue.add(b)
                }
              def onSubscribe(subscription: Flow.Subscription): Unit =
                printDebugMessage(s"onSubscribe:subscribed: ${subscription}")
                subscription.request(Long.MaxValue) // unbounded subscription
                isDone.set(false)
                err.updateAndGet(_ => null)
            }
        },
        4096
      )
    }

  given fileDownloadHttpResponseMapper(using Path, Seq[OpenOption]): HttpResponseMapper[Path] =
    new HttpResponseMapper[Path] {
      def bodyHandler = BodyHandlers.ofFile(summon[Path], summon[Seq[OpenOption]]: _*)
    }
