package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Symbols.*
import munit.FunSuite

class CallsSuspendContinuationSuite extends FunSuite, CompilerFixtures:

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(Right(1)) } should be Some(mySuspend)") {
    case (given Context, defdef) =>
      // because this is a subtree projection, we cannot use tree
      // equality on the returned trees, as the rightOne fixture and
      // rightOne instance in the embedded tree are not the same tree by
      // reference equality. We can use NoDiff on the printed returned
      // tree, however, since we know we do not modify the inner tree in
      // the extractor.
      assertNoDiff(CallsSuspendContinuation.unapply(defdef).get.toString, defdef.toString)
  }

  compilerContextWithContinuationsPlugin.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = 10 should be None") {
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
        tpd.Literal(Constant(10))
      )

      assertEquals(CallsSuspendContinuation.unapply(d), None)
  }
