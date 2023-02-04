package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.config.Printers
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Symbols.{newSymbol, ClassSymbol}
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
        
      // format: off
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
           |    def foo(completion: continuations.Continuation[Int | Any]): Any = 1
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expectedOutput)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should work when there are no continuations") {
    case given Context =>
      val source = """|class A""".stripMargin
      // format: off
      val expected = """|package <empty> {
        |  @SourceFile("compileFromString.scala") class A() extends Object() {}
        |}
        |""".stripMargin
      // format: on
      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should work when there are no continuations".fail) {
    case given Context =>
      val source = """|class A""".stripMargin
      val expected = """|package <empty> {
                        |  @SourceFile("compileFromString.scala") class B() extends Object() {}
                        |}""".stripMargin
      checkContinuations(source) {
        case (tree, _) =>
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
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |}
           |""".stripMargin
      checkCompile("pickleQuotes", source) {
        case (tree, _) =>
          assertEquals(tree.toString, """|""".stripMargin)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should convert a suspended def with a single constant and a non suspended body to CPS"
  ) {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def foo(x: Int)(using Suspend): Int = x + 1
          |""".stripMargin

      // format: off
      val expected =
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
           |    def foo(x: Int, completion: continuations.Continuation[Int | Any]): Any = x.+(1)
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
    "It should convert a suspended def with a single constant and a non suspended body to CPS"
  ) {
    case given Context =>
      val source =
        """
          |import continuations.*
          |import scala.concurrent.ExecutionContext
          |
          |def foo(x: Int, z: String*)(using s: Suspend, ec: ExecutionContext): Int = x + 1
          |""".stripMargin

      // format: off
      val expected =
        """|package <empty> {
           |  import continuations.*
           |  import scala.concurrent.ExecutionContext
           |  final lazy module val compileFromString$package: 
           |    compileFromString$package
           |   = new compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class 
           |    compileFromString$package
           |  () extends Object() { this: compileFromString$package.type =>
           |    private def writeReplace(): AnyRef = 
           |      new scala.runtime.ModuleSerializationProxy(classOf[compileFromString$package.type])
           |    def foo(x: Int, z: Seq[String] @Repeated, ec: concurrent.ExecutionContext, completion: continuations.Continuation[Int | Any]): Any = x.+(1)
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContext.test("It should run the compiler") {
    case given Context =>
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
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int](continuation => continuation.resume(Right(1)))
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
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int] { _.resume(Right(1)) }
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
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int](_.resume(Right(1)))
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
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Left(new Exception("error"))) }
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
    "it should convert simple suspended def with no parameters with multiple expressions in the body") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int]{ c =>
           |    println("Hello")
           |    c.resume(Right(1))
           |    val x = 1
           |    val y = false
           |    c.resume(Right(2))
           |    println(x)
           |  }
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
           |        println("Hello")
           |        safeContinuation.resume(Right.apply[Nothing, Int](1))
           |        val x: Int = 1
           |        val y: Boolean = false
           |        safeContinuation.resume(Right.apply[Nothing, Int](2))
           |        println(x)
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
    "it should convert simple suspended def with a named implicit Suspend") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using s: Suspend): Int =
           |  s.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
    "it should convert simple suspended def with a context function that has Suspend") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo(): Suspend ?=> Int =
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
    "it should convert simple suspended def with no parameters keeping the rows before the suspend call") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  val x = 5
           |  println("HI")
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
           |        {
           |          val continuation1: continuations.Continuation[Int] = completion
           |          val safeContinuation: continuations.SafeContinuation[Int] = 
           |            new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
           |              continuations.Continuation.State.Undecided
           |            )
           |          safeContinuation.resume(Right.apply[Nothing, Int](1))
           |          safeContinuation.getOrThrow()
           |        }
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
    "it should even convert suspended def with no parameters that doesn't call resume") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int] { _ => val x = 1; println("Hello") }
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
           |        val x: Int = 1
           |        println("Hello")
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
    "it should convert simple suspended def with a single constant parameter with a Right input") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo(x: Int)(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x + 1)) }
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
           |    def foo(x: Int, completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
           |      {
           |        val continuation1: continuations.Continuation[Int] = completion
           |        val safeContinuation: continuations.SafeContinuation[Int] = 
           |          new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
           |            continuations.Continuation.State.Undecided
           |          )
           |        safeContinuation.resume(Right.apply[Nothing, Int](x.+(1)))
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
    "it should convert a zero arity definition that contains a suspend call but returns a non-suspending value " +
      "into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |    10
           |  }
           |  foo()
           |}
           |""".stripMargin

      val sourceContextFunction =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo():Suspend ?=> Int = {
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |    10
           |  }
           |  foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |            10
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }

      checkContinuations(sourceContextFunction) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert a >0 arity definition that contains a suspend call but returns a non-suspending value " +
      "into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo(x: Int)(using Suspend): Int = {
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(x)) }
           |    10
           |  }
           |  foo(11)
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(null, this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(x: Int, completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](x))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |            10
           |          }
           |        foo(11, continuations.jvm.internal.ContinuationStub.contImpl)
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
    "it should convert simple nested suspended def with a single parameter keeping the rest of the rows untouched") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo(x: Int)(using Suspend): Int = {
           |  val y = 5
           |  println("HI")
           |  if (x < y) then {
           |    x
           |  } else {
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |  }
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
           |    def foo(x: Int, completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = 
           |      {
           |        val y: Int = 5
           |        println("HI")
           |        if x.<(y) then 
           |          {
           |            x
           |          }
           |         else 
           |          {
           |            {
           |              val continuation1: continuations.Continuation[Int] = completion
           |              val safeContinuation: continuations.SafeContinuation[Int] = 
           |                new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int](continuation1)(), 
           |                  continuations.Continuation.State.Undecided
           |                )
           |              safeContinuation.resume(Right.apply[Nothing, Int](1))
           |              safeContinuation.getOrThrow()
           |            }
           |          }
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
    "it should convert simple suspended def with no parameters with multiple `suspendContinuation` that returns " +
      "a non-suspending value into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |    summon[Suspend].suspendContinuation[Boolean] { continuation => continuation.resume(Right(false)) }
           |    summon[Suspend].suspendContinuation[String] { continuation => continuation.resume(Right("Hello")) }
           |    10
           |  }
           |
           |foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label1] ()
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label1[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 2
           |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
           |                      new continuations.SafeContinuation[Boolean](
           |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
           |                      , continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label2] ()
           |                    ()
           |                  case 2 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label2[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 3
           |                    val safeContinuation: continuations.SafeContinuation[String] = 
           |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
           |                        , 
           |                      continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    ()
           |                  case 3 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |            10
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
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
    "it should convert simple suspended def with no parameters with multiple `suspendContinuation` that returns " +
      "a non-suspending value and with multiple expressions in the suspendContinuation body into a state machine " +
      "including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    summon[Suspend].suspendContinuation[Int] { continuation =>
           |      println("Hello")
           |      println("World")
           |      continuation.resume(Right(1))
           |    }
           |    summon[Suspend].suspendContinuation[Boolean] { continuation =>
           |      continuation.resume(Right(false))
           |      continuation.resume(Right(true))
           |    }
           |    summon[Suspend].suspendContinuation[String] { continuation =>
           |      continuation.resume(Right("Hello"))
           |      val x = 1
           |    }
           |    10
           |  }
           |
           |foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    println("Hello")
           |                    println("World")
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label1] ()
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label1[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 2
           |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
           |                      new continuations.SafeContinuation[Boolean](
           |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
           |                      , continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
           |                    safeContinuation.resume(Right.apply[Nothing, Boolean](true))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label2] ()
           |                    ()
           |                  case 2 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label2[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 3
           |                    val safeContinuation: continuations.SafeContinuation[String] = 
           |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
           |                        , 
           |                      continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
           |                    val x: Int = 1
           |                    ()
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    ()
           |                  case 3 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |            10
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
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
    "it should convert a zero arity definition that contains suspend " +
      "calls with expressions between them and returns a non-suspending value into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    println("Start")
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |    val x = "World"
           |    println("Hello")
           |    println(x)
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
           |    println("End")
           |    10
           |  }
           |  foo()
           |}
           |""".stripMargin

      val sourceContextFunction =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo(): Suspend ?=> Int = {
           |    println("Start")
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |    val x = "World"
           |    println("Hello")
           |    println(x)
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(2)) }
           |    println("End")
           |    10
           |  }
           |  foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    println("Start")
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label1] ()
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label1[Unit]: <empty>
           |                    val x: String = "World"
           |                    println("Hello")
           |                    println(x)
           |                    $continuation.asInstanceOf[program$foo$1].$label = 2
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](2))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    ()
           |                  case 2 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |            println("End")
           |            10
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }

      checkContinuations(sourceContextFunction) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with no parameters with multiple `suspendContinuation` " +
      "into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    summon[Suspend].suspendContinuation[Boolean] { continuation => continuation.resume(Right(false)) }
           |    summon[Suspend].suspendContinuation[String] { continuation => continuation.resume(Right("Hello")) }
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |  }
           |
           |foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
           |                      new continuations.SafeContinuation[Boolean](
           |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
           |                      , continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label1] ()
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label1[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 2
           |                    val safeContinuation: continuations.SafeContinuation[String] = 
           |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
           |                        , 
           |                      continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label2] ()
           |                    ()
           |                  case 2 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label2[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 3
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    orThrow
           |                  case 3 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
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
    "it should convert simple suspended def with no parameters with multiple `suspendContinuation` " +
      "and with multiple expressions in the suspendContinuation body into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    summon[Suspend].suspendContinuation[Boolean] { continuation =>
           |      println("Hi")
           |      continuation.resume(Right(false))
           |    }
           |    summon[Suspend].suspendContinuation[String] {
           |      continuation => continuation.resume(Right("Hello"))
           |      println("World")
           |    }
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |  }
           |
           |foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
           |                      new continuations.SafeContinuation[Boolean](
           |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
           |                      , continuations.Continuation.State.Undecided)
           |                    println("Hi")
           |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label1] ()
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label1[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 2
           |                    val safeContinuation: continuations.SafeContinuation[String] = 
           |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
           |                        , 
           |                      continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
           |                    println("World")
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label2] ()
           |                    ()
           |                  case 2 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label2[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 3
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    orThrow
           |                  case 3 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
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
    "it should convert simple suspended def with no parameters with multiple `suspendContinuation` " +
      "and with expressions between them into a state machine including an update of the call site:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program: Int = {
           |  def foo()(using Suspend): Int = {
           |    println("Start")
           |    val x = 1
           |    summon[Suspend].suspendContinuation[Boolean] { continuation => continuation.resume(Right(false)) }
           |    println("Hello")
           |    summon[Suspend].suspendContinuation[String] { continuation => continuation.resume(Right("Hello")) }
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |  }
           |
           |foo()
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
           |    def program: Int = 
           |      {
           |        class program$foo$1($completion: continuations.Continuation[Any | Null]) extends continuations.jvm.internal.ContinuationImpl($completion, 
           |          $completion.context
           |        ) {
           |          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
           |          var $label: Int = _
           |          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
           |             = 
           |          ()
           |          def $label_=(x$0: Int): Unit = ()
           |          protected override def invokeSuspend(
           |            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]
           |          ): Any | Null = 
           |            {
           |              this.$result = result
           |              this.$label = this.$label.|(scala.Int.MinValue)
           |              foo(this.asInstanceOf[continuations.Continuation[Int]])
           |            }
           |        }
           |        def foo(completion: continuations.Continuation[Int]): 
           |          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)
           |         = 
           |          {
           |            {
           |              var $continuation: continuations.Continuation[Any] | Null = null
           |              completion match 
           |                {
           |                  case x$0 @ <empty> if x$0.isInstanceOf[program$foo$1].&&(x$0.asInstanceOf[program$foo$1].$label.&(scala.Int.MinValue).!=(0)) => 
           |                    $continuation = x$0.asInstanceOf[program$foo$1]
           |                    $continuation.asInstanceOf[program$foo$1].$label = $continuation.asInstanceOf[program$foo$1].$label.-(scala.Int.MinValue)
           |                  case _ => $continuation = new program$foo$1(completion.asInstanceOf[continuations.Continuation[Any | Null]])
           |                }
           |              val $result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)] = 
           |                $continuation.asInstanceOf[program$foo$1].$result
           |              $continuation.asInstanceOf[program$foo$1].$label match 
           |                {
           |                  case 0 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    println("Start")
           |                    val x: Int = 1
           |                    $continuation.asInstanceOf[program$foo$1].$label = 1
           |                    val safeContinuation: continuations.SafeContinuation[Boolean] = 
           |                      new continuations.SafeContinuation[Boolean](
           |                        continuations.intrinsics.IntrinsicsJvm$package.intercepted[Boolean]($continuation)()
           |                      , continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, Boolean](false))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label1] ()
           |                    ()
           |                  case 1 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label1[Unit]: <empty>
           |                    println("Hello")
           |                    $continuation.asInstanceOf[program$foo$1].$label = 2
           |                    val safeContinuation: continuations.SafeContinuation[String] = 
           |                      new continuations.SafeContinuation[String](continuations.intrinsics.IntrinsicsJvm$package.intercepted[String]($continuation)()
           |                        , 
           |                      continuations.Continuation.State.Undecided)
           |                    safeContinuation.resume(Right.apply[Nothing, String]("Hello"))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    return[label2] ()
           |                    ()
           |                  case 2 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                    label2[Unit]: <empty>
           |                    $continuation.asInstanceOf[program$foo$1].$label = 3
           |                    val safeContinuation: continuations.SafeContinuation[Int] = 
           |                      new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(), 
           |                        continuations.Continuation.State.Undecided
           |                      )
           |                    safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                    val orThrow: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) = 
           |                      safeContinuation.getOrThrow()
           |                    if orThrow.==(continuations.Continuation.State.Suspended) then return continuations.Continuation.State.Suspended
           |                    orThrow
           |                  case 3 => 
           |                    if $result.!=(null) then 
           |                      $result.fold[Unit](
           |                        {
           |                          def $anonfun(x$0: Throwable): Nothing = throw x$0
           |                          closure($anonfun)
           |                        }
           |                      , 
           |                        {
           |                          def $anonfun(x$0: Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)): Unit = ()
           |                          closure($anonfun)
           |                        }
           |                      )
           |                     else ()
           |                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
           |                }
           |            }
           |          }
           |        foo(continuations.jvm.internal.ContinuationStub.contImpl)
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
    "it should transform the method parameters adding a completion one and updating the call site when there is " +
      "a `using Suspend` parameter") {
    case given Context =>
      val sourceNoSuspend =
        """|
           |package continuations
           |
           |import scala.concurrent.ExecutionContext
           |import concurrent.ExecutionContext.Implicits.global
           |
           |def program: Int = {
           |  def foo[A, B](x: A, y: B)(z: String)(using s: Suspend, ec: ExecutionContext): A = x
           |  foo(1,2)("A")
           |}
           |""".stripMargin

      val sourceSuspend =
        """|
           |package continuations
           |
           |import scala.concurrent.ExecutionContext
           |import concurrent.ExecutionContext.Implicits.global
           |
           |def program: Int = {
           |  def foo[A, B](x: A, y: B)(z: String)(using s: Suspend, ec: ExecutionContext): A =
           |    summon[Suspend].suspendContinuation[A] { continuation => continuation.resume(Right(x)) }
           |  foo(1,2)("A")
           |}
           |""".stripMargin

      // format: off
      val expectedNoSuspend =
        """|
           |package continuations {
           |  import scala.concurrent.ExecutionContext
           |  import concurrent.ExecutionContext.Implicits.global
           |  final lazy module val compileFromString$package: 
           |    continuations.compileFromString$package
           |   = new continuations.compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class 
           |    compileFromString$package
           |  () extends Object() { this: continuations.compileFromString$package.type =>
           |    private def writeReplace(): AnyRef = 
           |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
           |    def program: Int = 
           |      {
           |        def foo(x: A, y: B, z: String, ec: concurrent.ExecutionContext, completion: continuations.Continuation[A | Any]): Any = x
           |        foo(1, 2, "A", concurrent.ExecutionContext.Implicits.global, continuations.jvm.internal.ContinuationStub.contImpl)
           |      }
           |  }
           |}
           |""".stripMargin

      val expectedSuspend =
        """|
           |package continuations {
           |  import scala.concurrent.ExecutionContext
           |  import concurrent.ExecutionContext.Implicits.global
           |  final lazy module val compileFromString$package: 
           |    continuations.compileFromString$package
           |   = new continuations.compileFromString$package()
           |  @SourceFile("compileFromString.scala") final module class 
           |    compileFromString$package
           |  () extends Object() { this: continuations.compileFromString$package.type =>
           |    private def writeReplace(): AnyRef = 
           |      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
           |    def program: Int = 
           |      {
           |        def foo(x: A, y: B, z: String, ec: concurrent.ExecutionContext, completion: continuations.Continuation[A]): 
           |          Any | Null | continuations.Continuation.State.Suspended.type
           |         = 
           |          {
           |            val continuation1: continuations.Continuation[A] = completion
           |            val safeContinuation: continuations.SafeContinuation[A] = 
           |              new continuations.SafeContinuation[A](continuations.intrinsics.IntrinsicsJvm$package.intercepted[A](continuation1)(), 
           |                continuations.Continuation.State.Undecided
           |              )
           |            safeContinuation.resume(Right.apply[Nothing, A](x))
           |            safeContinuation.getOrThrow()
           |          }
           |        foo(1, 2, "A", concurrent.ExecutionContext.Implicits.global, continuations.jvm.internal.ContinuationStub.contImpl)
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(sourceNoSuspend) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expectedNoSuspend)
      }

      checkContinuations(sourceSuspend) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expectedSuspend)
      }
  }
}
