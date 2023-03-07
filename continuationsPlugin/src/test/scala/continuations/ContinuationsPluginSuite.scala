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
class ContinuationsPluginSuite extends FunSuite, CompilerFixtures, StateMachineFixtures {

  compilerContextWithContinuationsPlugin.test(
    """|it should transform a 0-arity suspended definition returning a
       |non-blocking value into a definition accepting a continuation
       |returning the non-blocking value""".stripMargin) {
    case given Context =>
      val source =
        """|import continuations.*
           |
           |def program: Unit = {
           | def foo()(using Suspend): Int = 1
           | println(foo())
           |}""".stripMargin
        
      // format: off
      val expectedOutput =
        """package <empty> {
          |  import continuations.*
          |  final lazy module val compileFromString$package:
          |    compileFromString$package
          |   = new compileFromString$package()
          |  @SourceFile("compileFromString.scala") final module class
          |    compileFromString$package
          |  () extends Object() { this: compileFromString$package.type =>
          |    private def writeReplace(): AnyRef =
          |      new scala.runtime.ModuleSerializationProxy(classOf[compileFromString$package.type])
          |    def program: Unit =
          |      {
          |        def foo(completion: continuations.Continuation[Int | Any]): Any = 1
          |        println(foo(continuations.jvm.internal.ContinuationStub.contImpl))
          |      }
          |  }
          |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            expectedOutput)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    """|it should transform a 0-arity suspended definition without empty parameters list returning a
       |non-blocking value into a definition accepting a continuation
       |returning the non-blocking value""".stripMargin) {
    case given Context =>
      val source =
        """| import continuations.*
           |
           |def program: Unit = {
           | def foo(using Suspend): Int = 1
           | println(foo)
           |}""".stripMargin

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
           |    def program: Unit =
           |      {
           |        def foo(completion: continuations.Continuation[Int | Any]): Any = 1
           |        println(foo(continuations.jvm.internal.ContinuationStub.contImpl))
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            expectedOutput)
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
    "It should convert a suspended context function def with a single constant and a non suspended body to CPS"
  ) {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def foo(x: Int): Suspend ?=> Int = x + 1
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
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expected))
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
           |    def foo(x: Int, z: String*, ec: concurrent.ExecutionContext, completion: continuations.Continuation[Int | Any]): Any = x.+(1)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedOneSuspendContinuationTwoBlocks)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedOneSuspendContinuation)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedOneSuspendContinuation)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedOneSuspendContinuation)
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
           |        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |        {
           |          {
           |            safeContinuation.resume(Left.apply[Exception, Nothing](new Exception("error")))
           |          }
           |        }
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
           |        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |        {
           |          {
           |            println("Hello")
           |            safeContinuation.resume(Right.apply[Nothing, Int](1))
           |            val x: Int = 1
           |            val y: Boolean = false
           |            safeContinuation.resume(Right.apply[Nothing, Int](2))
           |            println(x)
           |          }
           |        }
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
    "it should convert simple suspended def with no parameters with vals inside resume") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |
           |def foo()(using Suspend): Int =
           |  summon[Suspend].suspendContinuation[Int]{ c =>
           |    c.resume{
           |      println("Hello")
           |      val x = 1
           |      val y = 1
           |      Right(x + y)
           |    }
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
           |        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |        {
           |          {
           |            safeContinuation.resume(
           |              {
           |                println("Hello")
           |                val x: Int = 1
           |                val y: Int = 1
           |                Right.apply[Nothing, Int](x.+(y))
           |              }
           |            )
           |          }
           |        }
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedOneSuspendContinuationTwoBlocks)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedOneSuspendContinuationTwoBlocks)
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
           |          val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |          {
           |            {
           |              safeContinuation.resume(Right.apply[Nothing, Int](1))
           |            }
           |          }
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
           |        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |        {
           |          {
           |            val x: Int = 1
           |            println("Hello")
           |          }
           |        }
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
           |        val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |        {
           |          {
           |            safeContinuation.resume(Right.apply[Nothing, Int](x.+(1)))
           |          }
           |        }
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
           |  def foo(): Suspend ?=> Int = {
           |    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |    10
           |  }
           |  foo()
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineForSuspendContinuationReturningANonSuspendingVal)
      }

      checkContinuations(sourceContextFunction) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineForSuspendContinuationReturningANonSuspendingVal)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineWithSingleSuspendedContinuationReturningANonSuspendedVal)
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
           |              val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int](continuation1)
           |              {
           |                {
           |                  safeContinuation.resume(Right.apply[Nothing, Int](1))
           |                }
           |              }
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineMultipleSuspendedContinuationsReturningANonSuspendingVal)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineWithMultipleResumeReturningANonSuspendedValue)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineReturningANonSuspendedValue)
      }

      checkContinuations(sourceContextFunction) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineReturningANonSuspendedValue)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineNoDependantSuspensions)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineNoDependantSuspensionsWithCodeInside)
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

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            compileSourceIdentifier.replaceAllIn(tree.show, ""),
            expectedStateMachineNoDependantSuspensionsWithCodeBetween)
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
           |            val safeContinuation: continuations.SafeContinuation[A] = continuations.SafeContinuation.init[A](continuation1)
           |            {
           |              {
           |                safeContinuation.resume(Right.apply[Nothing, A](x))
           |              }
           |            }
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
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expectedNoSuspend))
      }

      checkContinuations(sourceSuspend) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expectedSuspend))
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should transform a dependent suspendContinuation with one param into a state machine") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo(x: Int)(using Suspend): Int = {
           |  val y = summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) }
           |  x + y
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expectedStateMachineOneParamOneDependantContinuation)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should transform a suspend continuation with one parameter into a state machine"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo(qq: Int)(using s: Suspend): Int = {
           |  summon[Suspend].suspendContinuation[Unit] { _.resume(Right { println(qq) }) }
           |  10
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expectedStateMachineOneParamOneNoDependantContinuation)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should transform a suspend continuation with no parameter and code before the continuation " +
      "that is used afterwards into a state machine"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def foo()(using s: Suspend): Int = {
           |  val xx = 111
           |  println(xx)
           |  summon[Suspend].suspendContinuation[Int] { _.resume(Right( 10 )) }
           |  xx
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(
              expectedStateMachineNoParamOneNoDependantContinuationCodeBeforeUsedAfter)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should transform a suspend continuation with many dependant continuations " +
      "with code around them to a state machine"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(qq: Int)(using s: Suspend): Int = {
           |    val pp = 11
           |    val xx = s.suspendContinuation[Int] { _.resume(Right { qq - 1 }) }
           |    val ww = 13
           |    val rr = "AAA"
           |    val yy = s.suspendContinuation[String] { _.resume(Right { rr }) }
           |    val tt = 100
           |    val zz = s.suspendContinuation[Int] { _.resume(Right { ww - 1 }) }
           |    println(xx)
           |    xx + qq + yy.size + zz + pp + tt
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expectedStateMachineManyDependantContinuations)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should transform a suspend continuation with many dependant and no dependant continuations " +
      "with code around them to a state machine"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(qq: Int)(using s: Suspend): Int = {
           |    val pp = 11
           |    val xx = s.suspendContinuation[Int] { _.resume(Right { qq - 1 }) }
           |    val ww = 13
           |    val rr = "AAA"
           |    s.suspendContinuation[String] { _.resume(Right { rr }) }
           |    val tt = 100
           |    val zz = s.suspendContinuation[Int] { _.resume(Right { ww - 1 }) }
           |    println(xx)
           |    xx + qq + zz + pp + tt
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(
              expectedStateMachineManyDependantAndNoDependantContinuations)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should transform a suspend continuation with dependant and no dependant continuation at the end" +
      "with code around them and inside to a state machine"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int)(using s: Suspend): Int = {
           |    println("Hello")
           |    val y = s.suspendContinuation[Int] { _.resume(Right { println("World"); 1 }) }
           |    val z = 1
           |    s.suspendContinuation[Int] { continuation =>
           |      val w = "World"
           |      println("Hello")
           |      continuation.resume(Right { println(z); x })
           |    }
           |    val tt = 2
           |    x + y + tt
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(
              expectedStateMachineWithDependantAndNoDependantContinuationAtTheEnd)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert into a state machine for one chained suspend continuation"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest()(using s: Suspend): Int = {
           |    val x = s.suspendContinuation[Int] { _.resume(Right { 1 }) }
           |    s.suspendContinuation[Int] { _.resume(Right { x + 1 }) }
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expectedStateMachineForOneChainedContinuation)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert into a state machine multiple chained continuations with" +
      "code in between and returning a non suspended value"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(q: Int)(using s: Suspend): Int = {
           |    println("Hello")
           |    val z = 100
           |    val x = s.suspendContinuation[Int] { _.resume(Right { 1 + z}) }
           |    val j = 9
           |    val w = s.suspendContinuation[Int] { _.resume(Right { x + 1 + q}) }
           |    println("World")
           |    s.suspendContinuation[Int] { _.resume(Right { x + w + 1 + j}) }
           |    10
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(
              expectedStateMachineMultipleChainedSuspendContinuationsReturningANonSuspendedVal)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should update the call site for a simple suspended def with a generic param ") {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def program = {
           |  case class Foo(i: Int)
           |  def foo[A](a: A)(using Suspend): A = a
           |  foo(Foo(1))
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
           |    def program: Object = 
           |      {
           |        case class Foo(i: Int) extends Object(), _root_.scala.Product, _root_.scala.Serializable {
           |          override def hashCode(): Int = 
           |            {
           |              var acc: Int = -889275714
           |              acc = scala.runtime.Statics#mix(acc, this.productPrefix.hashCode())
           |              acc = scala.runtime.Statics#mix(acc, Foo.this.i)
           |              scala.runtime.Statics#finalizeHash(acc, 1)
           |            }
           |          override def equals(x$0: Any): Boolean = 
           |            this.eq(x$0.$asInstanceOf[Object]).||(
           |              x$0 match 
           |                {
           |                  case x$0 @ _:Foo @unchecked => this.i.==(x$0.i).&&(x$0.canEqual(this))
           |                  case _ => false
           |                }
           |            )
           |          override def toString(): String = scala.runtime.ScalaRunTime._toString(this)
           |          override def canEqual(that: Any): Boolean = that.isInstanceOf[Foo @unchecked]
           |          override def productArity: Int = 1
           |          override def productPrefix: String = "Foo"
           |          override def productElement(n: Int): Any = 
           |            n match 
           |              {
           |                case 0 => this._1
           |                case _ => throw new IndexOutOfBoundsException(n.toString())
           |              }
           |          override def productElementName(n: Int): String = 
           |            n match 
           |              {
           |                case 0 => "i"
           |                case _ => throw new IndexOutOfBoundsException(n.toString())
           |              }
           |          val i: Int
           |          def copy(i: Int): Foo = new Foo(i)
           |          def copy$default$1: Int @uncheckedVariance = Foo.this.i
           |          def _1: Int = this.i
           |        }
           |        final lazy module val Foo: Foo = new Foo()
           |        final module class Foo() extends AnyRef(), scala.deriving.Mirror.Product { this: Foo.type =>
           |          def apply(i: Int): Foo = new Foo(i)
           |          def unapply(x$1: Foo): Foo = x$1
           |          override def toString: String = "Foo"
           |          type MirroredMonoType = Foo
           |          def fromProduct(x$0: Product): continuations.compileFromString$package.Foo.MirroredMonoType = 
           |            new Foo(x$0.productElement(0).$asInstanceOf[Int])
           |        }
           |        def foo(a: A, completion: continuations.Continuation[A | Any]): Any = a
           |        foo(Foo.apply(1), continuations.jvm.internal.ContinuationStub.contImpl):Object & Product & Serializable
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
    "it should convert into a state machine chained continuations with an input parameter"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int)(using Suspend): Int = {
           |    val y = summon[Suspend].suspendContinuation[Int] { continuation =>
           |      continuation.resume(Right(x + 1))
           |    }
           |    summon[Suspend].suspendContinuation[Int] { continuation =>
           |      continuation.resume(Right(y + 1))
           |    }
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(
              expectedStateMachineChainedSuspendContinuationsOneParameter)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "it should convert into a state machine chained continuations with an input parameter and vals"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int)(using Suspend): Int = {
           |    val q = 2
           |    val w = 3
           |    val y = summon[Suspend].suspendContinuation[Int] { continuation =>
           |      continuation.resume(Right(x + w))
           |    }
           |    val p = 1
           |    val t = 1
           |    val z = summon[Suspend].suspendContinuation[Int] { continuation =>
           |      continuation.resume(Right(y + q + x))
           |    }
           |    z + y + p
           |}
           |""".stripMargin

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(
              expectedStateMachineChainedSuspendContinuationsOneParameterAndVals)
          )
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should convert a polymorphic function with suspended context and a non suspended body to CPS"
  ) {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def foo: Suspend ?=> Int => Int = x => x + 1
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
           |    def foo(completion: continuations.Continuation[(Int => Int) | Any]): Int => Any =
           |      {
           |        def $anonfun(x: Int): Any = x.+(1)
           |        closure($anonfun)
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expected))
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should convert a polymorphic function value with suspended context param a non suspended body to CPS"
  ) {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |val foo:  Suspend ?=> [A] => List[A] => Int = [A] => (list: List[A]) => list.size
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
           |    def foo(completion: continuations.Continuation[([A] => (List[A]) => Int) | Any]): [A] => (List[A]) => Any =
           |      {
           |        final class $anon() extends Object(), PolyFunction {
           |          def apply[A >: Nothing <: Any](list: List[A]): Int = list.size
           |        }
           |        new $anon():([A] => (List[A]) => Int)
           |      }
           |  }
           |}
           |""".stripMargin
      // format: on

      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(
            removeLineTrailingSpaces(compileSourceIdentifier.replaceAllIn(tree.show, "")),
            removeLineTrailingSpaces(expected))
      }
  }
}
