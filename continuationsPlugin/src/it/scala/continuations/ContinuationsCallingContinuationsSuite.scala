package continuations

import munit.SnapshotSuite
import dotty.tools.dotc.core.Contexts.Context

class ContinuationsCallingContinuationsSuite extends SnapshotSuite, CompilerFixtures {

  compilerContextWithContinuationsPlugin.snapshotTest(
    "A suspended function calling another suspended function will compile and work") {
    case given Context =>
      val source = """|package examples
                      |import continuations.*
                      |def NonSuspendingContinuationCallsOtherContinuation = {
                      |  def cont2(x: Int)(using Suspend): Int = x + 1
                      |  def cont1(x: Int)(using Suspend): Int = cont2(x + 2)
                      |  println(cont1(1))
                      |}
                      |""".stripMargin
      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest(
    "A suspended function calling another suspended function that suspends will compile and work") {
    case given Context =>
      val source = """|package examples
                      |import continuations.*
                      |def NonSuspendingContinuationCallsOtherContinuation = {
                      |  def cont2(x: Int)(using s:Suspend): Int = s.shift(_.resume(x + 1))
                      |  def cont1(x: Int)(using s:Suspend): Int = cont2(x + 2)
                      |
                      |  println(cont1(1))
                      |}
                      |""".stripMargin
      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest(
    "A suspended function that suspends calling another suspended function that suspends will compile and work") {
    case given Context =>
      val source =
        """|package examples
           |import continuations.*
           |def NonSuspendingContinuationCallsOtherContinuation = {
           |  def cont2(x: Int)(using s:Suspend): Int = s.shift(_.resume(x + 1))
           |  def cont1(x: Int)(using s:Suspend): Int = s.shift(_.resume(cont2(x + 2)))
           |
           |  println(cont1(1))
           |}
           |""".stripMargin
      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest(
    "A suspended function that suspends calling another suspended function that suspends multiple times will compile and work") {
    case given Context =>
      val source =
        """|package examples
           |import continuations.*
           |def NonSuspendingContinuationCallsOtherContinuation = {
           |  def cont2(x: Int)(using s:Suspend): Int ={
           |
           |    val y = s.shift[Int](_.resume(x + 1))
           |    val z = s.shift[Int](_.resume(y + 1))
           |    y + z
           |  }
           |  def cont1(x: Int)(using s:Suspend): Int = s.shift(_.resume(cont2(x + 2)))
           |
           |  println(cont1(1))
           |}
           |""".stripMargin
      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest(
    "A suspended function that suspends multiple times calling another suspended function that suspends multiple times will compile and work") {
    case given Context =>
      val source = """|package examples
                      |import continuations.*
                      |def NonSuspendingContinuationCallsOtherContinuation = {
                      |  def cont2(x: Int)(using s:Suspend): Int ={
                      |
                      |    val y = s.shift[Int](_.resume(x + 1))
                      |    val z = s.shift[Int](_.resume(y + 1))
                      |    y + z
                      |  }
                      |  def cont1(x: Int)(using s:Suspend): Int = {
                      |    val y = s.shift[Int](_.resume(cont2(x + 2)))
                      |    val z = s.shift[Int](_.resume(y + 1))
                      |    y + z
                      |  }
                      |
                      |  println(cont1(1))
                      |}
                      |""".stripMargin
      continuationsCompilerSnapshot(source)
  }

}
