package munit

import munit.DisciplineSuite
import cats.effect.laws.AsyncLaws
import fx.instances.{FxAsync, StructuredF}
import cats.implicits._
import cats.effect.implicits._
import org.scalacheck.{Arbitrary, Gen}
import cats.effect.laws.AsyncTests
import cats.effect.kernel.instances.*
import cats.effect.kernel.testkit.SyncTypeGenerators.arbitrarySyncType

object StructurredFLaws extends DisciplineSuite {

  import FxAsync.{given, *}

  given arbInt[A: Arbitrary]: Arbitrary[StructuredF[A]] = ???

  checkAll("StructuredF.AsyncLaws", AsyncTests[StructuredF].async[Int, Int, String])
}
