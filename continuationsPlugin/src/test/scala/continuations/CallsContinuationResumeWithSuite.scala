package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*
import munit.FunSuite

class CallsContinuationResumeWithSuite extends FunSuite, CompilerFixtures:

  continuationsContextAndZeroAritySuspendSuspendingDefDefAndRightOne.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) } should be Some(tree) " +
      "where true == Right(1)") {
    case (given Context, defdef, rightOne) =>
      // because this is a subtree projection, we cannot use tree
      // equality on the returned trees, as the rightOne fixture and
      // rightOne instance in the embedded tree are not the same tree by
      // reference equality. We can use NoDiff on the printed returend
      // tree, however, since we know we do not modify the inner tree in
      // the extractor.
      assertEquals(
        CallsContinuationResumeWith.unapply(defdef).get.map(_.toString),
        List(rightOne).map(_.toString))
  }

  compilerContextWithContinuationsPlugin.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "summon[Suspend].suspendContinuation[Int] { continuation => () } should be None") {
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
            TypeApply(
              Apply(
                ref(suspend).select(termName("suspendContinuation")),
                List(ref(suspend))
              ),
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
