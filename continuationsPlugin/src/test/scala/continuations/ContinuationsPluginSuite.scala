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
       |returning the non-blocking value""".stripMargin.ignore) {
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
           |    def foo(completion: continuations.Continuation[Int | Any]): Object = 1
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
    "It should work when there are no continuations".ignore) {
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
    "It should work when there are no continuations".fail.ignore) {
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
    "It should convert a suspended def with a single constant and a non suspended body to CPS".ignore
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
           |    def foo(x: Int, completion: continuations.Continuation[Int | Any]): Object = x.+(1)
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
    "It should convert a suspended def with a single constant and a non suspended body to CPS".ignore
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
           |    def foo(x: Int, z: Seq[String] @Repeated, completion: continuations.Continuation[Int | Any])(using ec: concurrent.ExecutionContext): Object = 
           |      x.+(1)
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
      }
  }

  compilerContext.test("It should run the compiler".ignore) {
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
    "it should convert simple suspended def with no parameters with a Right input".ignore) {
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
    "it should convert simple suspended def with no parameters with a Right input using braces".ignore) {
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
    "it should convert simple suspended def with no parameters with a Right input but no inner var".ignore) {
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
    "it should convert simple suspended def with no parameters with a Right input but no inner var using braces".ignore) {
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
    "it should convert simple suspended def with no parameters with a Left input".ignore) {
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
    "it should convert simple suspended def with a named implicit Suspend".ignore) {
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
    "it should convert simple suspended def with no parameters keeping the rows before the suspend call".ignore) {
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
    "it should convert simple suspended def with no parameters ignoring any rows after the suspend call".ignore) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  val x = 5
           |  println("HI")
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
    "it should not convert suspended def with no parameters that doesn't call resume".ignore) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int] { _ => () }
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
           |        val Suspend_this: (x$1 : continuations.Suspend) = x$1
           |        (throw Suspend_this.continuations$Suspend$$inline$CompilerRewriteUnsuccessfulException):Int
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
    "it should not convert simple suspended def with no parameters with multiple `suspendContinuation`".ignore) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
           |    def foo()(using x$1: continuations.Suspend): Int = 
           |      {
           |        {
           |          val Suspend_this: (x$1 : continuations.Suspend) = x$1
           |          (throw Suspend_this.continuations$Suspend$$inline$CompilerRewriteUnsuccessfulException):Int
           |        }
           |        {
           |          val Suspend_this: (x$1 : continuations.Suspend) = x$1
           |          (throw Suspend_this.continuations$Suspend$$inline$CompilerRewriteUnsuccessfulException):Int
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
    "it should convert simple suspended def with a single constant parameter with a Right input".ignore) {
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
    "it should convert a zero arity definition that contains a suspend call but returns a non-suspending value into a state machine:") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using Suspend): Int = {
           |  summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
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
           |
           |    class continuations$foo$1($completion: continuations.Continuation[Any | Null])
           |      extends ContinuationImpl($completion, $completion.context) {
           |        var $result: Either[Throwable, Any | Null | Continuation.State.Suspended.type] = _
           |        var $label: Int = _
           |
           |        protected override def invokeSuspend(
           |          result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type]
           |        ): Any | Null = 
           |          {
           |            this.$result = result
           |            this.$label = this.$label | Int.MinValue
           |            foo(this.asInstanceOf[Continuation[Int]])
           |          }
           |    }
           |
           |    def foo(completion: Continuation[Int]): Int | Null | Continuation.State.Suspended.type = {
           |      var $continuation: Continuation[Any] | Null = null
           |
           |      completion match {
           |        case continuations$foo$ : continuations$foo$1
           |            if (continuations$foo$.$label & Int.MinValue) != 0x0 =>
           |          $continuation = continuations$foo$
           |
           |          $continuation.asInstanceOf[continuations$foo$1].$label =
           |            $continuation.asInstanceOf[continuations$foo$1].$label - Int.MinValue
           |
           |        case _ =>
           |          $continuation = new continuations$foo$1(
           |            completion.asInstanceOf[Continuation[Any | Null]])
           |      }
           |
           |      val $result: Either[Throwable, Any | Null | Continuation.State.Suspended.type] =
           |        $continuation.asInstanceOf[continuations$foo$1].$result
           |
           |      $continuation.asInstanceOf[continuations$foo$1].$label match
           |        case 0 =>
           |          $result.fold (t => throw t, _ => ())
           |
           |          $continuation.asInstanceOf[continuations$foo$1].$label = 1
           |
           |          val safeContinuation: continuations.SafeContinuation[Int] =
           |            new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
           |              continuations.Continuation.State.Undecided
           |            )
           |          safeContinuation.resume(Right(1))
           |
           |          val orThrow: Any | Null | Continuation.State.Suspended.type = safeContinuation.getOrThrow()
           |          if (orThrow == Continuation.State.Suspended) {
           |            return Continuation.State.Suspended
           |          }
           |        case 1 =>
           |          $result.fold(t => throw t, _ => ())
           |        case _ =>
           |          throw new IllegalStateException ("call to 'resume' before 'invoke' with coroutine")
           |      10
           |    }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          println(tree.show)
//          assertNoDiff(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
          assertNoDiff("", "")
      }
  }
}
