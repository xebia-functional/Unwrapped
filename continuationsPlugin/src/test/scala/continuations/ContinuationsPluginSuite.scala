package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.config.Printers
import dotty.tools.dotc.core.Contexts.{Context, ctx}
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Symbols.{ClassSymbol, newSymbol}
import dotty.tools.dotc.core.{Flags, Names}
import dotty.tools.dotc.core.Flags.EmptyFlags
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Types.*
import munit.FunSuite

/**
 * TODO: Add the compiler core libraries and scala standard library to the classpath
 * @see
 *   [[https://stackoverflow.com/questions/4713031/how-to-use-scalatest-to-develop-a-compiler-plugin-in-scala]]
 */
class ContinuationsPluginSuite extends FunSuite, CompilerFixtures {

  compilerContextWithContinuationsPlugin.test(
    """|it should transform a 0-arity suspended definition returning a
       |non-blocking value into a definition accepting a continuation
       |returning the non-blocking value""".stripMargin) {
    case given Context =>
      val source =
        """| import continuations.*
           | def foo()(using Suspend): Int = 1""".stripMargin
      val expectedOutput =
        """|package <empty> {
           |  import continuations.*
           |  final lazy module val compileFromString$package:
           |    compileFromString$package
           |   = new compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class
           |    compileFromString$package
           |  () extends Object() { this: compileFromString$package.type =>
           |    private def writeReplace(): AnyRef =
           |      new scala.runtime.ModuleSerializationProxy(classOf[compileFromString$package.type])
           |    def foo(completion: continuations.Continuation[Int | Any]): Object = 1
           |  }
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, ctx) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expectedOutput)
      }
  }

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

  compilerContext.test("debug".ignore) {
    case given Context =>
      val source =
        """|package continuations
           |
           |def foo()(using Suspend): Int = {
           |  val x = 5
           |  println("HI")
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |}
           |""".stripMargin
      checkCompile("pickleQuotes", source) {
        case (tree, ctx) =>
          assertEquals(tree.toString(), """|""".stripMargin)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should convert a suspended def with a single constant and a non suspended body to CPS"
  ) { implicit givenContext =>
    val source =
      """
        |package continuations
        |
        |def foo(x: Int)(using Suspend): Int = x + 1
        |""".stripMargin

    val expected =
      """
        |package continuations {
        |  final lazy module val compileFromString$package:
        |    continuations.compileFromString$package
        |   = new continuations.compileFromString$package()
        |  @SourceFile("compileFromString.scala") final module class
        |    compileFromString$package
        |  () extends Object() { this: continuations.compileFromString$package.type =>
        |    private def writeReplace(): AnyRef =
        |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
        |    def foo(x: Int)(using x$2: continuations.Suspend): Int = x.+(1)
        |  }
        |}
        |""".stripMargin

    checkContinuations(source) {
      case (tree, _) =>
        assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "returnsContextFunctionWithSuspendType should return true for a context function with a call to suspend"
  ) { implicit givenContext =>

    val source =
      """
        |package continuations
        |
        |def foo(x: Int): Suspend ?=> Int =
        |  Continuation.suspendContinuation { (c: Continuation[Int]) =>
        |    c.resume(Right(x))
        |    Continuation.State.Suspended
        |  }
        |
        |""".stripMargin

    checkContinuations(source) {
      case (tree, context) =>
        given Context = context

        val funName = Names.termName("$anonfun")

        val defDefs = tree.filterSubTrees {
          case DefDef(name, _, _, _) if name == funName => true
          case _ => false
        }

        val foo = defDefs.head.asInstanceOf[tpd.DefDef]

        val sut = new ContinuationsPhase
        assertEquals(sut.returnsContextFunctionWithSuspendType(foo), true)
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "returnsContextFunctionWithSuspendType should return false for a context function with no call to suspend"
  ) { implicit givenContext =>

    val source =
      """
        |package continuations
        |
        |def foo(x: Int): Suspend ?=> Int = x + 1
        |
        |""".stripMargin

    checkContinuations(source) {
      case (tree, context) =>
        given Context = context

        val funName = Names.termName("$anonfun")

        val defDefs = tree.filterSubTrees {
          case DefDef(name, _, _, _) if name == funName => true
          case _ => false
        }

        val foo = defDefs.head.asInstanceOf[tpd.DefDef]

        val sut = new ContinuationsPhase
        assertEquals(sut.returnsContextFunctionWithSuspendType(foo), false)
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "returnsContextFunctionWithSuspendType should return true for a function using Suspend with a call to suspend"
  ) { implicit givenContext =>

    val source =
      """
        |package continuations
        |
        |def foo(x: Int)(using s: Suspend): Int =
        |  Continuation.suspendContinuation { (c: Continuation[Int]) =>
        |    c.resume(Right(x))
        |    Continuation.State.Suspended
        |  }
        |
        |""".stripMargin

    checkContinuations(source) {
      case (tree, context) =>
        given Context = context

        val funName = Names.termName("foo")

        val defDefs = tree.filterSubTrees {
          case DefDef(name, _, _, _) if name == funName => true
          case _ => false
        }

        val foo = defDefs.head.asInstanceOf[tpd.DefDef]

        val sut = new ContinuationsPhase
        assertEquals(sut.returnsContextFunctionWithSuspendType(foo), true)
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "returnsContextFunctionWithSuspendType should return false for a function using Suspend with no call to suspend"
  ) { implicit givenContext =>

    val source =
      """
        |package continuations
        |
        |def foo(x: Int)(using s: Suspend): Int = x + 1
        |
        |""".stripMargin

    checkContinuations(source) {
      case (tree, context) =>
        given Context = context

        val funName = Names.termName("foo")

        val defDefs = tree.filterSubTrees {
          case DefDef(name, _, _, _) if name == funName => true
          case _ => false
        }

        val foo = defDefs.head.asInstanceOf[tpd.DefDef]

        val sut = new ContinuationsPhase
        assertEquals(sut.returnsContextFunctionWithSuspendType(foo), false)
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "returnsContextFunctionWithSuspendType should return false for a function with Suspend as a parameter"
  ) { implicit givenContext =>

    val source =
      """
        |package continuations
        |
        |def foo(x: Int)(s: Suspend): Int = x + 1
        |
        |""".stripMargin

    checkContinuations(source) {
      case (tree, context) =>
        given Context = context

        val funName = Names.termName("foo")

        val defDefs = tree.filterSubTrees {
          case DefDef(name, _, _, _) if name == funName => true
          case _ => false
        }

        val foo = defDefs.head.asInstanceOf[tpd.DefDef]

        val sut = new ContinuationsPhase
        assertEquals(sut.returnsContextFunctionWithSuspendType(foo), false)
    }
  }

  compilerContext.test("It should run the compiler") { implicit givenContext =>
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

      case _ => fail(s"no list or context compiled from source ${source}, ${types}")
    }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters with a Right input") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters with a Right input using braces") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int](continuation => continuation.resume(Right(1)))
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
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters with a Right input but no inner var") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int] { _.resume(Right(1)) }
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
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters with a Right input but no inner var using braces") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int](_.resume(Right(1)))
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
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters with a Left input") {
    implicit givenContext =>

      val source =
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
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters keeping the rows before the suspend call") {
    implicit givenContext =>

      val source =
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
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters ignoring any rows after the suspend call") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  val x = 5
           |  println("HI")
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |  10
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
  }

  compilerContextWithContinuationsPlugin.test(
    "it should not convert suspended def with no parameters that doesn't call resume") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  Continuation.suspendContinuation[Int] { _ => () }
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
           |    def foo()(using x$1: continuations.Suspend): Int = ??? :Int
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should not convert simple suspended def with no parameters with multiple `suspendContinuation`") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
           |    def foo()(using x$1: continuations.Suspend): Int = 
           |      {
           |        ??? :Int
           |        ??? :Int
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }
}
