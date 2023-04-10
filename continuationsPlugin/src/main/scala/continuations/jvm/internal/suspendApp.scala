package continuations.jvm.internal

import continuations.*
import continuations.intrinsics.startContinuation
import continuations.jvm.internal

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CountDownLatch, Executors, ThreadFactory}
import scala.concurrent
import scala.concurrent.ExecutionContext

object SuspendApp:
  private var result: Either[Throwable, Int] = Right(0)
  def apply[A](block: Thing ?=> A): Unit =

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
    // val pool: ExecutionContext = ExecutionContext.global
    val latch: CountDownLatch = CountDownLatch(1)
    println(s"Thread 0 suspendApp: ${Thread.currentThread().getName}")
    pool.execute {
      new Runnable:
        override def run(): Unit =
          println(s"Thread 1 suspendApp: ${Thread.currentThread().getName}")
          val baseCont = BuildContinuation[A](pool, { res =>
            res.fold(t => throw t, _ => println(s"Thread builder continuation: ${Thread.currentThread().getName}"))
            latch.countDown()
          })
          block.startContinuation(baseCont/*ContinuationStub.potato*/)
          println(s"Thread 2 suspendApp: ${Thread.currentThread().getName}")
    }
    latch.await()
    result.fold(throw _, identity)
    println(s"Last thread suspendApp: ${Thread.currentThread().getName}")
