package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*
import munit.FunSuite

class CallsContinuationResumeWithSuite extends FunSuite, CompilerFixtures:

  continuationsContextAndZeroAritySuspendSuspendingDefDefAndRightOne.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) } should be Some(tree) " +
      "where tree == Right(1)") {
    case (given Context, defdef, rightOne) =>
      // because this is a subtree projection, we cannot use tree
      // equality on the returned trees, as the rightOne fixture and
      // rightOne instance in the embedded tree are not the same tree by
      // reference equality. We can use NoDiff on the printed returend
      // tree, however, since we know we do not modify the inner tree in
      // the extractor.
      assertNoDiff(
        CallsContinuationResumeWith.unapply(defdef).map(_.toString).get,
        Some(rightOne).map(_.toString).get)
  }

  compilerContextWithContinuationsPlugin.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "Continuation.suspendContinuation[Int] { continuation => () } should be None") {
    case given Context =>
      import tpd.*

      val suspend = requiredClass(suspendFullName)
      val continuation = requiredModule(continuationFullName)

      val intType = ctx.definitions.IntType

      val usingSuspend =
        newSymbol(ctx.owner, termName("x$1"), union(GivenOrImplicit, Param), suspend.info)

      val anonFunc =
        newAnonFun(ctx.owner, continuation.companionClass.info)

      val continuationVal = newSymbol(
        ctx.owner,
        termName("continuation"),
        EmptyFlags,
        continuation.companionClass.typeRef.appliedTo(intType)
      )

      val rhs =
        Inlined(
          Apply(
            Apply(
              TypeApply(
                ref(continuation).select(termName("suspendContinuation")),
                List(TypeTree(intType))),
              List(
                Block(
                  Nil,
                  Block(
                    List(
                      DefDef(
                        anonFunc,
                        List(List(continuationVal)),
                        ctx.definitions.UnitType,
                        Block(Nil, ref(ctx.definitions.UnitClass))
                      )),
                    Closure(Nil, ref(anonFunc), TypeTree(ctx.definitions.UnitType))
                  )
                ))
            ),
            List(ref(usingSuspend))
          ),
          List.empty,
          EmptyTree
        )

      val d = DefDef(
        newSymbol(ctx.owner, termName("mySuspend"), EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        rhs
      )

      assertEquals(CallsContinuationResumeWith.unapply(d), None)
  }

  compilerContext.test("It should return the resume argument if the continuation is reachabl") {
    implicit givenContext =>
      val source =
        """
          |package continuations
          |
          |def mySuspend()(using Suspend): Int =
          | val x = 3
          | println("Hi")
          | Continuation.suspendContinuation[Int] { continuation =>
          |   continuation.resume(Right(1))
          | }
          |""".stripMargin

      checkCompile("pickleQuotes", source) {
        case (tree, ctx) =>
          given Context = ctx

          val defDefs = tree.filterSubTrees {
            case DefDef(name, _, _, _) if name.show == "mySuspend" => true
            case _ => false
          }

          val foo = defDefs.head.asInstanceOf[tpd.DefDef]

          assertNoDiff(
            CallsContinuationResumeWith.unapply(foo).get.show,
            "Right.apply[Nothing, Int](1)")
      }
  }

  compilerContext.test("It should return None is the continuation isn't reachable") {
    implicit givenContext =>
      val source =
        """
          |package continuations
          |
          |def mySuspend()(using Suspend): Int =
          | val x = 3
          | println("Hi")
          | return x
          | Continuation.suspendContinuation[Int] { continuation =>
          |   continuation.resume(Right(1))
          | }
          |""".stripMargin

      checkCompile("pickleQuotes", source) {
        case (tree, ctx) =>
          given Context = ctx

          val defDefs = tree.filterSubTrees {
            case DefDef(name, _, _, _) if name.show == "mySuspend" => true
            case _ => false
          }

          val foo = defDefs.head.asInstanceOf[tpd.DefDef]

          assertEquals(CallsContinuationResumeWith.unapply(foo), None)
      }
  }

  compilerContext.test(
    "It should return the resume argument if the continuation is reachable even if there is a return") {
    implicit givenContext =>
      val source =
        """
          |package continuations
          |
          |def mySuspend()(using Suspend): Int =
          | val x = 3
          | println("Hi")
          | return Continuation.suspendContinuation[Int] { continuation =>
          |   continuation.resume(Right(1))
          | }
          |""".stripMargin

      checkCompile("pickleQuotes", source) {
        case (tree, ctx) =>
          given Context = ctx

          val defDefs = tree.filterSubTrees {
            case DefDef(name, _, _, _) if name.show == "mySuspend" => true
            case _ => false
          }

          val foo = defDefs.head.asInstanceOf[tpd.DefDef]

          assertNoDiff(
            CallsContinuationResumeWith.unapply(foo).get.show,
            "Right.apply[Nothing, Int](1)")
      }
  }
