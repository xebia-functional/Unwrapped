package fx

import _root_.cats.effect.IO
import _root_.cats.effect.implicits._
import _root_.cats.effect.kernel.Async
import _root_.cats.effect.kernel.Outcome
import _root_.cats.effect.kernel.instances.*
import _root_.cats.effect.kernel.testkit.AsyncGenerators
import _root_.cats.effect.kernel.testkit.GenK
import _root_.cats.effect.kernel.testkit.OutcomeGenerators
import _root_.cats.effect.kernel.testkit.SyncTypeGenerators.arbitrarySyncType
import _root_.cats.effect.laws.AsyncLaws
import _root_.cats.effect.laws.AsyncTests
import _root_.cats.implicits._
import _root_.cats.kernel.Eq
import _root_.cats.kernel.Order
import _root_.cats.laws.discipline.arbitrary._
import _root_.fx.Structured
import _root_.fx.instances.FxAsync
import _root_.fx.instances.StructuredF
import _root_.fx.structured
import fx.Structured
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.PropFromFun
import org.scalacheck.rng.Seed
import org.scalacheck.{Arbitrary => Arb}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import munit.fx.ScalaFXSuite
import munit.DisciplineSuite

class StructurredFLaws
    extends ScalaFXSuite,
      DisciplineSuite,
      AsyncGenerators[StructuredF],
      OutcomeGenerators {

  import FxAsync.*

  override implicit val arbitraryE: Arb[Throwable] = Arb.arbThrowable

  override implicit val F: Async[StructuredF] = FxAsync.asyncInstance

  override implicit val cogenE: Cogen[Throwable] = Cogen((seed, _) => seed.next)

  override protected implicit val cogenFU: Cogen[StructuredF[Unit]] =
    Cogen((seed, _) => seed.next)

  override protected implicit val arbitraryFD: Arb[FiniteDuration] = Arb.arbFiniteDuration

  override protected implicit val arbitraryEC: Arb[ExecutionContext] = Arb {
    Gen.const(ExecutionContext.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()))
  }

  given arbI: Arb[Int] = Arb.arbInt

  given Cogen[Int] = Cogen((seed, _) => seed.next)

  given cogenStructuredFA[A]: Cogen[StructuredF[A]] = Cogen((seed, _) => seed.next)

  given orderStructuredFFiniteDuration(
      using o: Order[FiniteDuration],
      s: Structured): Order[StructuredF[FiniteDuration]] =
    new Order[StructuredF[FiniteDuration]]:
      override def compare(
          x: StructuredF[FiniteDuration],
          y: StructuredF[FiniteDuration]): Int =
        Order[FiniteDuration].compare(x(using s), y(using s))

  given Eq[ExecutionContext] with
    override def eqv(x: ExecutionContext, y: ExecutionContext): Boolean = x === y

  given eqfa[A: Eq](using s: Structured): Eq[StructuredF[A]] = new Eq[StructuredF[A]] {
    override def eqv(x: StructuredF[A], y: StructuredF[A]): Boolean =
      Eq[A].eqv(x(using s), y(using s))
  }

  given Eq[Throwable] with
    override def eqv(x: Throwable, y: Throwable): Boolean =
      x.getMessage == y.getMessage && (x.getCause == null && y.getCause == null) || x
        .getCause
        .getMessage == y.getCause.getMessage

  given arbInt(using Structured): Arb[StructuredF[Int]] = Arb {
    Gen.posNum[Int].map[Structured ?=> Int](identity)
  }

  given arbStrin(using Structured): Arb[StructuredF[String]] = Arb {
    Gen.alphaLowerStr.map[Structured ?=> String](identity)
  }

  given arbUnit(using Structured): Arb[StructuredF[Unit]] = Arb {
    Arb.arbUnit.arbitrary.map[Structured ?=> Unit](identity)
  }

  given arbFAtoB(using Structured): Arb[StructuredF[Int => Int]] = Arb {
    Arb.arbFunction1[Int, Int].arbitrary.map[Structured ?=> Int => Int](identity)
  }

  given arbFBtoC(using Structured): Arb[StructuredF[Int => String]] = Arb {
    Arb.arbFunction1[Int, String].arbitrary.map[Structured ?=> Int => String](identity)
  }

  implicit def boolToProp(fa: StructuredF[Boolean])(using s: Structured): Prop = Prop(
    fa(using s))

  testFX("check eq uncancellable") {
    structured{
      val x: StructuredF[Int] = uncancellable(() => structured{fork(() => 3).join})
      val y: StructuredF[Int] = fork(() => 3).join

      assertFX(Eq[StructuredF[Int]].eqv(x, y))
    }
  }

  testFX("StructuredF.AsyncLaws") {
    checkAll(
      "StructuredF.AsyncLaws",
      structured {
        AsyncTests[StructuredF].async[Int, Int, String](FiniteDuration(500, "milliseconds"))
      })
  }
}
