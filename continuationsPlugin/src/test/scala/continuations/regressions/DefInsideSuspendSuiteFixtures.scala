package continuations
package regressions

import munit.FunSuite
import scala.io.Source

trait DefInsideSuspendSuiteFixtures { self: FunSuite & CompilerFixtures =>

  private def resourceAsString(name: String): String =
    Source.fromResource(name).getLines().mkString("\n")

  val defInsideSuspendSource = FunFixture[String](
    setup = _ => resourceAsString("DefInsideSuspendSource.scala"),
    teardown = _ => ())

  val expectedDefInsideSuspendOutput = FunFixture[String](
    setup = _ => resourceAsString("DefInsideSuspendExpected.scala"),
    teardown = _ => ())

  val defInsideSuspendFixtures = FunFixture.map3(
    compilerContextWithContinuationsPlugin,
    defInsideSuspendSource,
    expectedDefInsideSuspendOutput)

}
