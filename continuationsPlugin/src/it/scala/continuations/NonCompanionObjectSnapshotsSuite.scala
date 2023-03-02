package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.SnapshotSuite

class NonCompanionObjectSnapshotsSuite extends SnapshotSuite, CompilerFixtures:

  compilerContextWithContinuationsPlugin.snapshotTest("test-1") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int)(using s: Suspend): Int = {
          |    s.suspendContinuation[Int] { continuation =>
          |      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
          |    }
          |
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    1 + method1(x) + method2(x) + method3(x) + method4(x) + z1 + z2 + z3 + z4
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-2") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  def continuations(x: Int)(using s: Suspend): Int = {
          |    def method1(x: Int) = x + 1
          |    val z1 = 1
          |
          |    s.suspendContinuation[Int] { continuation =>
          |      def method2(x: Int) = x
          |      val z2 = 1
          |
          |      continuation.resume(Right(method1(x) + method2(x) + z1 + z2 + 1))
          |    }
          |
          |    def method3(x: Int) = x + 1
          |    val z3 = 1
          |
          |    method3(x) + z1 + z3 + 1
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-3") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  def continuations(x: Int)(using s: Suspend): Int = {
          |    def method1(x: Int) = x + 1
          |    val z1 = 1
          |
          |    s.suspendContinuation[Int] { continuation =>
          |      def method2(x: Int) = x
          |      val z2 = 1
          |
          |      continuation.resume(Right(method1(x) + method2(x) + z1 + z2 + 1))
          |    }
          |
          |    def method3(x: Int) = x + 1
          |    val z3 = 1
          |
          |    method1(x) + method3(x) + z1 + z3 + 1
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-4") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int)(using s: Suspend): Int = {
          |    val result1 = s.suspendContinuation[Int] { continuation =>
          |      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
          |    }
          |
          |    s.suspendContinuation[Int] { continuation =>
          |      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
          |    }
          |
          |    val result2 = s.suspendContinuation[Int] { continuation =>
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1 + result1))
          |    }
          |
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    1 + method1(x) + method2(x) + method3(x) + method4(x) + z1 + z2 + z3 + z4 + result1 + result2
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-5") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int)(using s: Suspend): Int = {
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    val result1 = s.suspendContinuation[Int] { continuation =>
          |      def method5(x: Int) = 1
          |      val z5 = 1
          |
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method4(x) + method5(x) +
          |          z1 + z2 + z3 + z4 + z5 + 1))
          |    }
          |
          |    def method6(x: Int) = x + 1
          |    val z6 = 1
          |
          |    s.suspendContinuation[Int] { continuation =>
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method6(x) +
          |          z1 + z2 + z3 + z4 + z6 + 1))
          |    }
          |
          |    def method7(x: Int) = x + 1
          |    val z7 = 1
          |
          |    val result2 = s.suspendContinuation[Int] { continuation =>
          |      def method8(x: Int) = 1
          |      val z8 = 1
          |
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method7(x) +
          |          method8(x) + z1 + z2 + z3 + z4 + z6 + z7 + z8 + 1))
          |    }
          |
          |    def method9(x: Int) = x + 1
          |    val z9 = 1
          |
          |    method1(x) + method2(x) + method3(x) + method9(x)
          |      z1 + z2 + z3 + z4 + z6 + z7 + z9 + 1 + result1 + result2
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-6") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int)(using s: Suspend): Int = {
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    val result1 = s.suspendContinuation[Int] { continuation =>
          |      def method5(x: Int) = 1
          |      val z5 = 1
          |
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method4(x) + method5(x) +
          |          z1 + z2 + z3 + z4 + z5 + 1))
          |    }
          |
          |    def method6(x: Int) = x + 1
          |    val z6 = 1
          |
          |    s.suspendContinuation[Int] { continuation =>
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method4(x) + method6(x) +
          |          z1 + z2 + z3 + z4 + z6 + 1))
          |    }
          |
          |    def method7(x: Int) = x + 1
          |    val z7 = 1
          |
          |    val result2 = s.suspendContinuation[Int] { continuation =>
          |      def method8(x: Int) = 1
          |      val z8 = 1
          |
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method4(x) + method6(x) + method7(x) +
          |          method8(x) + z1 + z2 + z3 + z4 + z6 + z7 + z8 + 1))
          |    }
          |
          |    def method9(x: Int) = x + 1
          |    val z9 = 1
          |
          |    method1(x) + method2(x) + method3(x) + method4(x) + method6(x) + method7(x) + method9(x)
          |      z1 + z2 + z3 + z4 + z6 + z7 + z9 + 1 + result1 + result2
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-7") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int): Suspend ?=> Int = {
          |    summon[Suspend].suspendContinuation[Int] { continuation =>
          |      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
          |    }
          |
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    1 + method1(x) + method2(x) + method3(x) + method4(x) + z1 + z2 + z3 + z4
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-8") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  def continuations(x: Int): Suspend ?=> Int = {
          |    def method1(x: Int) = x + 1
          |    val z1 = 1
          |
          |    summon[Suspend].suspendContinuation[Int] { continuation =>
          |      def method2(x: Int) = 1
          |      val z2 = 1
          |
          |      continuation.resume(Right(method1(x) + method2(x) + z1 + z2 + 1))
          |    }
          |
          |    def method3(x: Int) = x + 1
          |    val z3 = 1
          |
          |    method3(x) + z1 + z3 + 1
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-9") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int): Suspend ?=> Int = {
          |    val result1 = summon[Suspend].suspendContinuation[Int] { continuation =>
          |      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
          |    }
          |
          |    summon[Suspend].suspendContinuation[Int] { continuation =>
          |      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
          |    }
          |
          |    val result2 = summon[Suspend].suspendContinuation[Int] { continuation =>
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1 + result1))
          |    }
          |
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    1 + method1(x) + method2(x) + method3(x) + method4(x) + z1 + z2 + z3 + z4 + result1 + result2
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("test-cf-10") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | println(ExampleObject.continuations(1))
          |}
          |
          |object ExampleObject {
          |  private def method1(x: Int) = x + 1
          |  private val z1 = 1
          |
          |  protected def method2(x: Int) = x + 1
          |  protected val z2 = 1
          |
          |  def method3(x: Int) = x + 1
          |  val z3 = 1
          |
          |  def continuations(x: Int): Suspend ?=> Int = {
          |    def method4(x: Int) = x + 1
          |    val z4 = 1
          |
          |    val result1 = summon[Suspend].suspendContinuation[Int] { continuation =>
          |      def method5(x: Int) = 1
          |      val z5 = 1
          |
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method4(x) + method5(x) +
          |          z1 + z2 + z3 + z4 + z5 + 1))
          |    }
          |
          |    def method6(x: Int) = x + 1
          |    val z6 = 1
          |
          |    summon[Suspend].suspendContinuation[Int] { continuation =>
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method6(x) +
          |          z1 + z2 + z3 + z4 + z6 + 1))
          |    }
          |
          |    def method7(x: Int) = x + 1
          |    val z7 = 1
          |
          |    val result2 = summon[Suspend].suspendContinuation[Int] { continuation =>
          |      def method8(x: Int) = 1
          |      val z8 = 1
          |
          |      continuation.resume(
          |        Right(method1(x) + method2(x) + method3(x) + method7(x) +
          |          method8(x) + z1 + z2 + z3 + z4 + z6 + z7 + z8 + 1))
          |    }
          |
          |    def method9(x: Int) = x + 1
          |    val z9 = 1
          |
          |    method1(x) + method2(x) + method3(x) + method9(x)
          |      z1 + z2 + z3 + z4 + z6 + z7 + z9 + 1 + result1 + result2
          |  }
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }
