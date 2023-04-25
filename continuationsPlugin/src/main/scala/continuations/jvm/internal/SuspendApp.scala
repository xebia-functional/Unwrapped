package continuations.jvm.internal

import continuations.*
import continuations.intrinsics.startContinuation
import continuations.jvm.internal

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CountDownLatch, Executors, ThreadFactory}
import scala.concurrent
import scala.concurrent.ExecutionContext
import scala.util.Right

object SuspendApp:
  var result: Either[Throwable, Any] = Right(0)
  def apply(block: Any): Any =

    val count: AtomicLong = new AtomicLong(0)
    val defaultFactory = Executors.defaultThreadFactory()

    val customFactory = new ThreadFactory:
      override def newThread(r: Runnable): Thread =
        val thread: Thread = defaultFactory.newThread(r)
        thread.setName(s"custom-thread-pool-${count.getAndIncrement()}")
        thread

    val pool = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(7, customFactory)
    )

    val latch: CountDownLatch = CountDownLatch(1)

    pool.execute {
      new Runnable:
        override def run(): Unit =
          val baseCont = BuildContinuation[Any](
            pool,
            { res =>
              result = res
              latch.countDown()
            })
          block.startContinuation(baseCont)
    }
    latch.await()
    result match
      case Left(e) => throw e
      case Right(Right(v)) => v
      case Right(v) => v
