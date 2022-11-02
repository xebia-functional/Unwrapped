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
    "It should work when there are no continuations".ignore) { implicit givenContext =>
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
    "It should work when there are no continuations".fail.ignore) { implicit givenContext =>
    val source = """|class A""".stripMargin
    val expected = """|package <empty> {
                      |  @SourceFile("compileFromString.scala") class B() extends Object() {}
                      |}""".stripMargin
    checkContinuations(source) {
      case (tree, ctx) =>
        assertEquals(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
    }

  }

  compilerContext.test("it should run the compiler".ignore) { implicit givenContext =>
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
    "it should convert simple suspended def with no parameters") { implicit givenContext =>

    // FROM
    def foo()(using Suspend): Int =
      Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }

    /*
     * Is the initial code in the task correct?
     * seems different from the Kotlin one `kotlin.coroutines.Continuation.kt`,
     * https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/coroutines/Continuation.kt#L142,
     * there is `suspendCoroutineUninterceptedOrReturn`.
     * Also return type cannot be Int
     *
     * https://github.com/arrow-kt/arrow/tree/main/arrow-libs/fx/arrow-fx-coroutines#suspendcoroutine
     * https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/src/kotlin/coroutines/SafeContinuationJvm.kt#L20
     */
    // TO
    def fooConverted(
        completion: Continuation[Int]): Any | Null | Continuation.State.Suspended.type = {
      import continuations.intrinsics.intercepted

      val continuation1: Continuation[Int] = completion
      val safeContinuation: SafeContinuation[Int] =
        SafeContinuation[Int](continuation1.intercepted(), Continuation.State.Undecided)
      val suspendContinuation = 0
      safeContinuation.resume(Right(Int.box(1)))
      safeContinuation.getOrThrow()
    }

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
         |    def foo(completion: continuations.Continuation[Int]): Any = 
         |      {
         |        val continuation1: continuations.Continuation[Int] = completion
         |        val safeContinuation: continuations.SafeContinuation[Int] = 
         |          continuations.SafeContinuation.apply[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
         |            continuations.Continuation.State.Undecided
         |          )
         |        val suspendContinuation: Int = 0
         |        continuations.SafeContinuation[Int]#resume(Right.apply[Nothing, Int](1))
         |        continuations.SafeContinuation[Int]#getOrThrow()
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
         |    def foo(completion: continuations.Continuation[Int]): Any = 
         |      {
         |        val continuation1: continuations.Continuation[Int] = completion
         |        val safeContinuation: continuations.SafeContinuation[Int] = 
         |          continuations.SafeContinuation.apply[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
         |            continuations.Continuation.State.Undecided
         |          )
         |        val suspendContinuation: Int = 0
         |        continuations.SafeContinuation[Int]#resume(Left.apply[Exception, Nothing](new Exception("error")))
         |        continuations.SafeContinuation[Int]#getOrThrow()
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
  }
}
