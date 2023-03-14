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
    "CallsContinuationResumeWith#apply(defDefTree): def mySuspend()(using Suspend): Int = " +
      "summon[Suspend].shift[Int] { continuation => continuation.resume(1) } should be Some(mySuspend)") {
    case (given Context, defdef) =>
      // because this is a subtree projection, we cannot use tree
      // equality on the returned trees, as the rightOne fixture and
      // rightOne instance in the embedded tree are not the same tree by
      // reference equality. We can use NoDiff on the printed returned
      // tree, however, since we know we do not modify the inner tree in
      // the extractor.
      assertEquals(CallsSuspendContinuation(defdef), true)
  }

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    "CallsContinuationResumeWith#apply(defDefTree): def mySuspend()(using Suspend): Int = 1 should be None") {
    case (given Context, defdef) =>
      assertEquals(CallsSuspendContinuation(defdef), false)
  }

  continuationsContextAndZeroAritySuspendSuspendingValDef.test(
    "CallsContinuationResumeWith#apply(valDefTree): val mySuspend: Suspend ?=> Int = " +
      "summon[Suspend].shift[Int] { continuation => continuation.resume(1) } should be Some(mySuspend)") {
    case (given Context, valdef) =>
      assertEquals(CallsSuspendContinuation(valdef), true)
  }

  continuationsContextAndZeroAritySuspendNonSuspendingValDef.test(
    "CallsContinuationResumeWith#apply(valDefTree): val mySuspend: Suspend ?=> Int = 10 should be None") {
    case (given Context, valdef) =>
      assertEquals(CallsSuspendContinuation(valdef), false)
  }
