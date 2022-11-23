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
    "It should work when there are no continuations") { implicit givenContext =>
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
    "It should work when there are no continuations".fail) { implicit givenContext =>
    val source = """|class A""".stripMargin
    val expected = """|package <empty> {
                      |  @SourceFile("compileFromString.scala") class B() extends Object() {}
                      |}""".stripMargin
    checkContinuations(source) {
      case (tree, _) =>
        assertEquals(compileSourceIdentifier.replaceAllIn(tree.show, ""), expected)
    }
  }

  compilerContext.test("debug".only) {
    case given Context =>
      val source = """|package continuations
                      |
                      |def foo5(x: Int): Int = {
                      |
                      |  val y = Continuation.suspendContinuation[Int] { continuation =>
                      |    continuation.resume(Right(x + 1))
                      |  }
                      |  x + y
                      |}""".stripMargin
      checkCompile("typer", source) {
        case (tree, ctx) =>
          assertEquals(tree.toString(), """|""".stripMargin)
      }
  }

  compilerContextWithContinuationsPlugin.test(
    "It should convert a suspended def with a single constant and a non suspended body to CPS"
  ) { implicit givenContext =>
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
    "It should convert a suspended def with a single constant and a non suspended body to CPS"
  ) { implicit givenContext =>
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

  compilerContextWithContinuationsPlugin.test(
    "it should convert simple suspended def with a single constant parameter with a Right input") {
    implicit givenContext =>

      val source =
        """|
           |package continuations
           |
           |def foo(x: Int)(using Suspend): Int =
           |  Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(x + 1)) }
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
           |        val suspendContinuation: Int = 0
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
    "it should compile a single arity definition containing a suspend call and a dependent calculation into a state machine:") {
    case given Context =>
      // format: off
      val source = """|package continuations
                      |
                      |def foo5(x: Int): Int = {
                      |
                      |  val y = Continuation.suspendContinuation[Int] { continuation =>
                      |    continuation.resume(Right(x + 1))
                      |  }
                      |  x + y
                      |}
                      |""".stripMargin
      /**
        * How to do labels and jumps -- you return from a labeld position to another label.
        *  Reference
        *  dotty/compiler/src/dotty/tools/dotc/transform/TailRec.scala: https://github.com/lampepfl/dotty/blob/main/tests/pos-with-compiler-cc/dotc/transform/TailRec.scala#L107
        * 
           ```DefDef(gcd,
             List(
               List(
                 ValDef(
                   x,
                   TypeTree[
                     TypeRef(
                       ThisType(
                         TypeRef(
                           NoPrefix,
                           module class scala
                         )
                       ),
                       class Int
                     )
                   ],
                   EmptyTree
                 ),
                 ValDef(
                   y,
                   TypeTree[
                     TypeRef(
                       ThisType(
                         TypeRef(
                           NoPrefix,
                           module class scala
                         )
                       ),
                       class Int
                     )
                   ],
                   EmptyTree
                 )
               )
             ),
             TypeTree[
               TypeRef(
                 ThisType(
                   TypeRef(
                     NoPrefix,
                     module class scala
                   )
                 ),
                 class Int
               )
             ],
             Block(
               List(
                 ValDef(
                   y$tailLocal1,
                   TypeTree[
                     TypeRef(
                       ThisType(
                         TypeRef(
                           NoPrefix,
                           module class scala
                         )
                       ),
                       class Int
                     )
                   ],
                   Ident(y)
                 ),
                 ValDef(
                   x$tailLocal1,
                   TypeTree[
                     TypeRef(
                       ThisType(
                         TypeRef(
                           NoPrefix,
                           module
                             class scala
                         )
                       ),
                       class Int
                     )
                   ],
                   Ident(
                     x
                   )
                 )
               ),
               WhileDo(
                 EmptyTree,
                 Labeled(
                   Bind(
                     tailLabel1,
                     EmptyTree
                   ),
                   Return(
                     Block(
                       List(),
                       If(
                         Apply(
                           Select(
                             Ident(y),
                             ==
                           ),
                           List(
                             Literal(
                               Constant(0)
                             )
                           )
                         ),
                         Ident(x),
                         Block(
                           List(
                             ValDef(
                               x$tailLocal1$tmp1,
                               TypeTree[
                                 TypeRef(
                                   ThisType(
                                     TypeRef(
                                       NoPrefix,
                                       module class scala
                                     )
                                   ),
                                   class Int
                                 )
                               ],
                               Ident(y)
                             ),
                             ValDef(
                               y$tailLocal1$tmp1,
                               TypeTree[
                                 TypeRef(
                                   ThisType(
                                     TypeRef(
                                       NoPrefix,
                                       module class scala
                                     )
                                   ),
                                   class Int
                                 )
                               ],
                               Apply(
                                 Select(
                                   Ident(x),
                                   %
                                 ),
                                 List(
                                   Ident(y)
                                 )
                               )
                             ),
                             Assign(
                               Ident(x$tailLocal1),
                               Ident(x$tailLocal1$tmp1)
                             ),
                             Assign(
                               Ident(y$tailLocal1),
                               Ident(y$tailLocal1$tmp1)
                             )
                           ),
                           Typed(
                             Return(
                               Literal(
                                 Constant(
                                   ()
                                 )
                               ),
                               Ident(tailLabel1)
                             ),
                             TypeTree[
                               TypeRef(
                                 ThisType(
                                   TypeRef(
                                     NoPrefix,
                                     module class scala
                                   )
                                 ),
                                 class Int)
                             ]
                           )
                         )
                       )
                     ),
                     Ident(gcd)
                   )
                 )
               )
             )
           )
           
           
           package continuations {
             final lazy module val ∙
               compileFromString-4e51bdbb-9913-465a-992f-1ec2923e786a.$package
             : ∙
               ∙
                 continuations.
                   compileFromString-4e51bdbb-9913-465a-992f-1ec2923e786a.$package
               ∙
              = ∙
               new ∙
                 ∙
                   continuations.
                     compileFromString-4e51bdbb-9913-465a-992f-1ec2923e786a.$package
                 ∙
               ()
             @SourceFile("compileFromString-4e51bdbb-9913-465a-992f-1ec2923e786a..scala") ∙
               final
              module class compileFromString-4e51bdbb-9913-465a-992f-1ec2923e786a.$package
               ()
              extends Object() {
               private def writeReplace(): Object = ∙
                 new scala.runtime.ModuleSerializationProxy(
                   classOf[
                     continuations.
                       compileFromString-4e51bdbb-9913-465a-992f-1ec2923e786a.$package
                   ]
                 )
               @tailrec def gcd(x: Int, y: Int): Int = ∙
                 {
                   var y$tailLocal1: Int = y
                   var x$tailLocal1: Int = x
                   while <empty> do ∙
                     tailLabel1[Unit]: ∙
                       return ∙
                         {
                           if y$tailLocal1.==(0) then x$tailLocal1 else ∙
                             {
                               val x$tailLocal1$tmp1: Int = y$tailLocal1
                               val y$tailLocal1$tmp1: Int = x$tailLocal1.%(y$tailLocal1)
                               x$tailLocal1 = x$tailLocal1$tmp1
                               y$tailLocal1 = y$tailLocal1$tmp1
                               (return[tailLabel1] ()):Int
                             }
                         }
                 }
             }
           }
           ```
       * 
       */
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
           |    class continuations$foo5$1($completion: continuations.Continuation[Int], var $input: Int, var $label: Int) extends BaseContinuationImpl($completion) {
           |      var $result: Either[Throwable, Any | Null | Continuation.State.Suspended.type] = _
           |      override protected def invokeSuspend(
           |        result: Either[Throwable, Any | Null | Continuation.State.Suspended.type]): Any | Null = {
           |        this.$result = result
           |        this.$label = this.$label | Int.MIN_VALUE
           |        foo5(0, this.asInstanceOf[Continuation[Int]])
           |      }
           |    }
           |    def foo5(x: Int, $completion: continuations.Continuation[Int]): Any | Null | continuations.Continuation.State.Suspended.type = {
           |      var $continuation: Continuation[Any] = _
           |      var $z: Int = 0
           |      if($completion.isInstanceOf[continuations$foo5$1]) return[$Label_0049] ()
           |      var $continuation1 = continuation.asInstanceOf[continuations$foo5$1]
           |      if(($continuation1.$label & Int.MIN_VALUE) != 0x0){
           |        val $continuation2 = $continuation1
           |        $continuation2.$label = $continuation2.$label - Int.MIN_VALUE
           |      } else return[$Label_0049] ()
           |      $Label_0049[Unit]: return {
           |        var $result = $continuation.asInstanceOf[continuations$foo5$1].$result
           |        var $orThrow: Object = _
           |        $continuation.asInstanceOf[continuations$foo5$1].$label match {
           |          case 0 =>
           |            $result.fold(t => throw t, _ => ())
           |            $continuation.asInstanceOf[continuations$foo5$1].$input = x
           |            
           |            $continuation.asInstanceOf[continuations$foo5$1].$label = 1
           |            val $safeContinuation: continuations.SafeContinuation[Int] = 
           |              new continuations.SafeContinuation[Int](continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($completion)(), 
           |                continuations.Continuation.State.Undecided
           |            )
           |            val $continuation2 = $safeContinuation
           |            $continuation2.resumeWith(Right(x + 1))
           |            val $o = $orThrow = $continuation2.getOrThrow()
           |            if($o == continuations.Continuation.State.Suspended)
           |              return continuations.Continuation.State.Suspended
           |            }
           |          case 1 =>
           |            $z = $continuation.asInstanceOf[continuations$foo5$1].$input
           |            $result.fold($t => throw $t, _ => ())
           |            $orThrow = $result
           |            $lbl32[Int]: return {
           |              val y = $orThrow.asInstanceOf[Int]
           |              $z.+(y)
           |            }
           |          case _ =>
           |            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine")
           |        }
           |      }
           |    }
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
