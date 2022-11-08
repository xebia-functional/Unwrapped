package continuations

import continuations.DefDefTransforms.{CallsContinuationResumeWith, HasSuspendParameter}
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Flags.EmptyFlags
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.{Constants, Flags, Names}
import munit.FunSuite

class DefDefTransformsSuite extends FunSuite, CompilerFixtures {
  compilerContextWithContinuationsPlugin.test(
    "HasSuspendParameter#unapply(defDefTree): def mySuspend()(using Suspend): Int = 1 should be Some(tree)") {
    case given Context =>
      val suspend = requiredClass("continuations.Suspend")
      val d = tpd.DefDef(
        newSymbol(
          ctx.owner,
          Names.termName("mySuspend"),
          EmptyFlags,
          ctx.definitions.IntType).asTerm,
        List(
          List(),
          List(
            newSymbol(
              ctx.owner,
              Names.termName("x$1"),
              Flags.union(Flags.GivenOrImplicit, Flags.Param),
              suspend.info))),
        ctx.definitions.IntType,
        tpd.Literal(Constant(1))
      )

      assertEquals(HasSuspendParameter.unapply(d), Some(d))
  }

  compilerContextWithContinuationsPlugin.test(
    "HasSuspendParameter#unapply(defDefTree): def mySuspend(s: Suspend): Int = 1 should be None") {
    case given Context =>
      val suspend = requiredClass("continuations.Suspend")
      val d = tpd.DefDef(
        newSymbol(
          ctx.owner,
          Names.termName("mySuspend"),
          EmptyFlags,
          ctx.definitions.IntType).asTerm,
        List(
          List(
            newSymbol(ctx.owner, Names.termName("s"), Flags.union(Flags.Param), suspend.info))),
        ctx.definitions.IntType,
        tpd.Literal(Constant(1))
      )

      assertEquals(HasSuspendParameter.unapply(d), None)
  }

  compilerContextWithContinuationsPlugin.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "Continuation.suspendContinuation[Int] { continuation => continuation.resume(Right(1)) } should be Some(tree) " +
      "where true == Right(1)") {
    case given Context =>
      import tpd.*

      val suspend = requiredClass("continuations.Suspend")
      val continuation = requiredModule("continuations.Continuation")
      val right = requiredModule("scala.util.Right")

      val intType = ctx.definitions.IntType

      val usingSuspend =
        newSymbol(
          ctx.owner,
          Names.termName("x$1"),
          Flags.union(Flags.GivenOrImplicit, Flags.Param),
          suspend.info)

      val anonFunc =
        newAnonFun(ctx.owner, continuation.companionClass.info)

      val continuationVal = newSymbol(
        ctx.owner,
        Names.termName("continuation"),
        EmptyFlags,
        continuation.companionClass.typeRef.appliedTo(intType)
      )

      val rightOne =
        ref(right)
          .select(termName("apply"))
          .appliedToTypes(List(ctx.definitions.ThrowableType, intType))
          .appliedTo(Literal(Constant(1)))

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
                        Block(
                          Nil,
                          ref(continuationVal).select(termName("resume")).appliedTo(rightOne))
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
        newSymbol(ctx.owner, Names.termName("mySuspend"), EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        rhs
      )

      assertEquals(CallsContinuationResumeWith.unapply(d), Some(rightOne))
  }

  compilerContextWithContinuationsPlugin.test(
    "CallsContinuationResumeWith#unapply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "Continuation.suspendContinuation[Int] { continuation => () } should be None") {
    case given Context =>
      import tpd.*

      val suspend = requiredClass("continuations.Suspend")
      val continuation = requiredModule("continuations.Continuation")
      val right = requiredModule("scala.util.Right")

      val intType = ctx.definitions.IntType

      val usingSuspend =
        newSymbol(
          ctx.owner,
          Names.termName("x$1"),
          Flags.union(Flags.GivenOrImplicit, Flags.Param),
          suspend.info)

      val anonFunc =
        newAnonFun(ctx.owner, continuation.companionClass.info)

      val continuationVal = newSymbol(
        ctx.owner,
        Names.termName("continuation"),
        EmptyFlags,
        continuation.companionClass.typeRef.appliedTo(intType)
      )

      val rightOne =
        ref(right)
          .select(termName("apply"))
          .appliedToTypes(List(ctx.definitions.ThrowableType, intType))
          .appliedTo(Literal(Constant(1)))

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
        newSymbol(ctx.owner, Names.termName("mySuspend"), EmptyFlags, intType).asTerm,
        List(List(), List(usingSuspend)),
        intType,
        rhs
      )

      assertEquals(CallsContinuationResumeWith.unapply(d), None)
  }
}
