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
import org.scalacheck.Gen
import _root_.cats.effect.laws.AsyncTests
import _root_.cats.effect.kernel.instances.*
import _root_.cats.effect.kernel.testkit.*
import _root_.cats.effect.kernel.testkit.SyncTypeGenerators.arbitrarySyncType
import _root_.cats.laws.discipline.arbitrary._
import fx.Structured
import org.scalacheck.{Arbitrary => Arb, Cogen}
import _root_.cats.effect.IO
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.scalacheck.rng.Seed
import _root_.cats.effect.kernel.testkit.OutcomeGenerators
import _root_.cats.effect.kernel.Outcome
import _root_.cats.kernel.Order
import scala.concurrent.duration.FiniteDuration
import org.scalacheck.PropFromFun
import org.scalacheck.Prop

class StructurredFLaws extends DisciplineFXSuite {

  import FxAsync.{given, *}

  def seedOf[A](seed: Seed, fa: StructuredF[A]): Seed = seed

  given arbCogen: Cogen[StructuredF[Int]] = Cogen[StructuredF[Int]](seedOf[Int](_, _))

  given arbCogenOutcome: Cogen[Outcome[StructuredF, Throwable, Int]] =
    OutcomeGenerators.cogenOutcome[StructuredF, Throwable, Int]

  given arbitraryExecutionContext: Arb[ExecutionContext] = Arb {
    for {
      ec <- Gen.const(
        ExecutionContext.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor())
      )
      _ = sys.addShutdownHook(ec.shutdown())
    } yield ec
  }

  given eqF[A: Eq](using struct: Structured): Eq[StructuredF[A]] = new Eq {
    def eqv(x: StructuredF[A], y: StructuredF[A]): Boolean =
      val xval: A | Any = run { structured { x } }
      val yval: A | Any = run { structured { y } }
      xval == yval
  }
  given eqF(using struct: Structured): Eq[Throwable] = new Eq {
    def eqv(x: Throwable, y: Throwable): Boolean =
      x.getClass == y.getClass && x.getMessage == y.getMessage
  }
  given eqEc(using struct: Structured): Eq[ExecutionContext] = new Eq {
    def eqv(x: ExecutionContext, y: ExecutionContext): Boolean =
      x === y
  }

  given orderStrucuteredFFiniteDuration(
      using Structured,
      Order[FiniteDuration]): Order[StructuredF[FiniteDuration]] =
    new Order[StructuredF[FiniteDuration]] {
      @unchecked
      def compare(x: StructuredF[FiniteDuration], y: StructuredF[FiniteDuration]): Int =
        val xval: FiniteDuration | Any = run { structured { x } }
        val yval: FiniteDuration | Any = run { structured { y } }

        (xval, yval) match {
          case (xf: FiniteDuration, yf: FiniteDuration) => Order[FiniteDuration].compare(xf, yf)
          case _ => 0
        }
    }

  given arbInt[A](using arbA: Arb[A], struct: Structured): Arb[StructuredF[A]] = Arb.apply {

    for {
      i <- arbA.arbitrary
      f = {
        val f: Structured ?=> A = i
        f
      }
    } yield f

  }

  @unchecked
  implicit def structuredFBoolProp(propF: StructuredF[Boolean])(
      using struc: Structured): Prop = {
    val x: Boolean | Any = run(structured { propF })

    x match {
      case y: Boolean => Prop(y)
      case _ => Prop(false)
    }
  }

  // testFX("check eq") {
  //   val x: StructuredF[Int] = fork(() => 3).join
  //   val y: StructuredF[Int] = fork(() => 3).join

  //   assertFX(structured { Eq[StructuredF[Int]].eqv(x, y) })
  // }

  checkAllLaws("StructuredF.AsyncLaws") {
     structured {
       AsyncTests[StructuredF].async[Int, Int, String](FiniteDuration(30, "seconds"))
     }
  }
}
