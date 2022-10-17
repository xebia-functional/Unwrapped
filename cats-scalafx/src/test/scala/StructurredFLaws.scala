package fx

import munit.fx.DisciplineFXSuite
import _root_.cats.effect.laws.AsyncLaws
import _root_.fx.instances.{FxAsync, StructuredF}
import _root_.fx.Structured
import _root_.cats.kernel.Eq
import _root_.fx.structured
import _root_.cats.implicits._
import _root_.cats.effect.laws.AsyncTests
import _root_.cats.effect.implicits._
import org.scalacheck.{Gen}
import _root_.cats.effect.laws.AsyncTests
import _root_.cats.effect.kernel.instances.*
import _root_.cats.effect.kernel.testkit.*
import _root_.cats.effect.kernel.testkit.SyncTypeGenerators.arbitrarySyncType
import _root_.cats.laws.discipline.arbitrary._
import fx.Structured
import org.scalacheck.{
  Arbitrary => Arb,
  Cogen
}
import _root_.cats.effect.IO
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.scalacheck.rng.Seed
import _root_.cats.effect.kernel.testkit.OutcomeGenerators
import _root_.cats.effect.kernel.Outcome
import _root_.cats.kernel.Order
import scala.concurrent.duration.FiniteDuration


class StructurredFLaws extends DisciplineFXSuite {

  import FxAsync.{given, *}

  def seedOf[A](seed: Seed, fa: StructuredF[A]): Seed = seed

  given arbCogen: Cogen[StructuredF[Int]] = Cogen[StructuredF[Int]](seedOf[Int](_, _))

  given arbCogenOutcome: Cogen[Outcome[StructuredF, Throwable, Int]] = OutcomeGenerators.cogenOutcome[StructuredF, Throwable, Int]

  given arbitraryExecutionContext: Arb[ExecutionContext] = Arb{
    for {
      threads <- Gen.choose(1, Runtime.getRuntime.availableProcessors - 1)
      ec <- Gen.oneOf(
        ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()),
        ExecutionContext.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor()),
        ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(threads)),
        ExecutionContext.fromExecutorService(Executors.newWorkStealingPool()),
      )
      _ = sys.addShutdownHook(ec.shutdown())
    } yield ec
  }

  given eqF[A:Eq](using struct: Structured): Eq[StructuredF[A]] = new Eq{
    def eqv(x: StructuredF[A], y: StructuredF[A]): Boolean =
      val xval: A | Any = run{ x }
      val yval: A | Any = run{ y }
      xval == yval
  }
  given eqF(using struct: Structured): Eq[Throwable] = new Eq{
    def eqv(x: Throwable, y: Throwable): Boolean =
      x.getClass == y.getClass && x.getMessage == y.getMessage
  }
  given eqEc(using struct: Structured): Eq[ExecutionContext] = new Eq{
    def eqv(x: ExecutionContext, y: ExecutionContext): Boolean =
      x === y
  }

  given orderStrucuteredFFiniteDuration(using Structured, Order[FiniteDuration]): Order[StructuredF[FiniteDuration]] = new Order[StructuredF[FiniteDuration]] {
    def compare(x: Order[StructuredF[FiniteDuration]], y: Order[StructuredF[FiniteDuration]]): Int =
      val x: FiniteDuration | Any = run{x}
      val y: FiniteDuration | Any = run{y}
      (x, y) match {
        case (xf: FiniteDuration, yf: FiniteDuration) => Order[FiniteDuration].compare(xf, yf)
        case _ => 0
      }
  }
  
  given arbInt[A](using arbA: Arb[A], struct: Structured): Arb[StructuredF[A]] = Arb.apply{
    
    for{
      i <- arbA.arbitrary
      f = {
        val f: Structured ?=> A = i
        f
      }
    } yield f

  }

  checkAllLaws("StructuredF.AsyncLaws"){
    structured{
      AsyncTests[StructuredF].async[Int, Int, String]
      // AsyncTests[IO].async[Int, Int, String]
    }

  }


  // checkAllLaws[Throwable](
  //   "StructuredF.AsyncLaws"
  // )(
  //   summon
  // ){
  //   structured{
  //     AsyncTests[StructuredF].async[Int, Int, String]
  //   }
  // }


    

}
