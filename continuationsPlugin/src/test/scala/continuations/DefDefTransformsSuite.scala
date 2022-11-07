package continuations

import continuations.DefDefTransforms.{CallsContinuationResumeWith, HasSuspendParameter}
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Symbols.{newAnonFun, requiredClass, requiredModule}
import dotty.tools.dotc.core.{Constants, Flags, Names, Symbols}
import munit.FunSuite

class DefDefTransformsSuite extends FunSuite, CompilerFixtures {
  compilerContextWithContinuationsPlugin.test(
    "#unapply(defDefTree): def mySuspend()(using Suspend): Int = 1 should be Some(tree)") {
    case given Context =>
      val c = summon[Context]
      val suspend = Symbols.requiredClass("continuations.Suspend")
      val d = tpd.DefDef(
        Symbols
          .newSymbol(
            c.owner,
            Names.termName("mySuspend"),
            Flags.EmptyFlags,
            c.definitions.IntType)
          .asTerm,
        List(
          List(),
          List(
            Symbols.newSymbol(
              c.owner,
              Names.termName("x$1"),
              Flags.union(Flags.GivenOrImplicit, Flags.Param),
              suspend.info))),
        c.definitions.IntType,
        tpd.Literal(Constants.Constant(1))
      )

      assertEquals(HasSuspendParameter.unapply(d), Some(d))
  }

  compilerContextWithContinuationsPlugin.test(
    "#unapply(defDefTree): def mySuspend(s: Suspend): Int = 1 should be None") {
    case given Context =>
      val c = summon[Context]
      val suspend = Symbols.requiredClass("continuations.Suspend")
      val d = tpd.DefDef(
        Symbols
          .newSymbol(
            c.owner,
            Names.termName("mySuspend"),
            Flags.EmptyFlags,
            c.definitions.IntType)
          .asTerm,
        List(
          List(Symbols
            .newSymbol(c.owner, Names.termName("s"), Flags.union(Flags.Param), suspend.info))),
        c.definitions.IntType,
        tpd.Literal(Constants.Constant(1))
      )

      assertEquals(HasSuspendParameter.unapply(d), None)
  }
}
