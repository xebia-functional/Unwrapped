package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.ClassSymbol
import munit.FunSuite

/**
 * TODO: Add the compiler core libraries and scala standard library to the classpath
 * @see
 *   [[https://stackoverflow.com/questions/4713031/how-to-use-scalatest-to-develop-a-compiler-plugin-in-scala]]
 */
class ContinuationsPluginSuite extends FunSuite, CompilerFixtures {

  compilerContextWithContinuationsPlugin.test(
    "It should work when there are no continuations") { implicit givenContext =>
    val source = """|class A""".stripMargin
    // format: off
    val expected = """|package <empty> {
       |  @SourceFile("compileFromString.scala") class A() extends Object() {}
       |}
       |""".stripMargin
    // format: on
    checkContinuations(source) {
      case (tree, ctx) =>
        val f: Byte => (String, Char) = b => (b.toInt.toHexString, b.toChar)
        assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
    }

  }
  compilerContextWithContinuationsPlugin.test(
    "It should work when there are no continuations".fail) { implicit givenContext =>
    val source = """|class A""".stripMargin
    val expected = """|package <empty> {
                      |  @SourceFile("compileFromString.scala") class B() extends Object() {}
                      |}""".stripMargin
    checkContinuations(source) {
      case (tree, ctx) =>
        assertEquals(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
    }

  }

  compilerContext.test("it should run the compiler") { implicit givenContext =>
    val source = """
                   |class A
                   |class B extends A
    """.stripMargin

    val types = List(
      "A",
      "B",
      "List[?]",
      "List[Int]",
      "List[AnyRef]",
      "List[String]",
      "List[A]",
      "List[B]"
    )

    checkTypes(source, types: _*) {
      case (List(a, b, lu, li, lr, ls, la, lb), context) =>
        given Context = context
        assert(b <:< a)
        assert(li <:< lu)
        assert(!(li <:< lr))
        assert(ls <:< lr)
        assert(lb <:< la)
        assert(!(la <:< lb))

      case _ => fail("no list or context compiled from source ${source}, ${types}")
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters using `transformSuspendNoParametersOneContinuationResume`") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |""".stripMargin

      val sourceLeft =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Left(new Exception("error"))) }
           |""".stripMargin

      val sourceRowsBefore =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  val x = 5
           |  println("HI")
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |}
           |""".stripMargin

      // format: off
      val expected =
        """|
           |package continuations {
           |  final lazy module val compileFromString$package: 
           |    continuations.compileFromString$package
           |   = new continuations.compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class 
           |    compileFromString$package
           |  () extends Object() { this: continuations.compileFromString$package.type =>
           |    private def writeReplace(): AnyRef = 
           |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
           |    def foo(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
           |      {
           |        val continuation1: continuations.Continuation[Int] = completion
           |        val safeContinuation: continuations.SafeContinuation[Int] = 
           |          new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
           |            continuations.Continuation.State.Undecided
           |          )
           |        val suspendContinuation: Int = 0
           |        safeContinuation.resume(Right.apply[Nothing, Int](1))
           |        safeContinuation.getOrThrow()
           |      }
           |  }
           |}
           |""".stripMargin

      val expectedLeft =
        """|
           |package continuations {
           |  final lazy module val compileFromString$package: 
           |    continuations.compileFromString$package
           |   = new continuations.compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class 
           |    compileFromString$package
           |  () extends Object() { this: continuations.compileFromString$package.type =>
           |    private def writeReplace(): AnyRef = 
           |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
           |    def foo(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
           |      {
           |        val continuation1: continuations.Continuation[Int] = completion
           |        val safeContinuation: continuations.SafeContinuation[Int] = 
           |          new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
           |            continuations.Continuation.State.Undecided
           |          )
           |        val suspendContinuation: Int = 0
           |        safeContinuation.resume(Left.apply[Exception, Nothing](new Exception("error")))
           |        safeContinuation.getOrThrow()
           |      }
           |  }
           |}
           |""".stripMargin

      val expectedRowsBefore =
        """|
           |package continuations {
           |  final lazy module val compileFromString$package: 
           |    continuations.compileFromString$package
           |   = new continuations.compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class 
           |    compileFromString$package
           |  () extends Object() { this: continuations.compileFromString$package.type =>
           |    private def writeReplace(): AnyRef = 
           |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
           |    def foo(completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
           |      {
           |        val x: Int = 5
           |        println("HI")
           |        val continuation1: continuations.Continuation[Int] = completion
           |        val safeContinuation: continuations.SafeContinuation[Int] = 
           |          new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
           |            continuations.Continuation.State.Undecided
           |          )
           |        val suspendContinuation: Int = 0
           |        safeContinuation.resume(Right.apply[Nothing, Int](1))
           |        safeContinuation.getOrThrow()
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }

      checkContinuations(sourceLeft) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expectedLeft)
      }

      checkContinuations(sourceRowsBefore) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expectedRowsBefore)
      }
  }
}
