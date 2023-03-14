package examples

import continuations.Continuation
import continuations.SafeContinuation
import continuations.Suspend

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import scala.util.Random

given ExecutorService = Executors.newWorkStealingPool()

@main def NonblockingNonwaitingResume =
  def zeroArgumentsZeroContinuations()(using Suspend): Int = 1

  def twoArgumentsOneContinuationsCFBefore(
      x: Int,
      y: Int): Suspend ?=> Int | Continuation.State.Suspended.type =
    println("twoArgumentsOneContinuationsCFBefore")
    val z = 1
    summon[Suspend].shift[Int] { c =>
      summon[ExecutorService].submit(new Runnable {
        override def run =
          val sleepTime = Random.between(10, 5000)
          println(s"sleepTime $sleepTime")
          Thread.sleep(sleepTime)
          c.resume(x + y + z)
      })
    }

  val mappedContinuations = (1 to 100).toList.map(twoArgumentsOneContinuationsCFBefore(1, _))
  println(mappedContinuations)
