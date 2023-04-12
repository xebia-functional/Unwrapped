package continuations
package types

import munit.FunSuite
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Flags

class ExampleTypeMapSuite extends FunSuite, CompilerFixtures, ExampleTypeMapSuiteFixtures {

  symbolMakerAndMethodTypeMakerAndCompiler.test(
    "it should remove using Suspend from a method's type and insert a continuation of the final result type as a new type list at the index prior to the Suspend type.") {
    case (makeSymbol, makeMethodType, given Context) =>
      val ctx = summon[Context]
      val usingSuspendSymbol = makeSymbol(
        NoSymbol,
        Names.termName("s"),
        Flags.GivenOrImplicit | Flags.LocalParam,
        requiredClass(suspendFullName).typeRef,
        None,
        None)
      val completionSymbol = makeSymbol(
        NoSymbol,
        Names.termName(completionParamName),
        Flags.LocalParam,
        requiredClass(continuationFullName).typeRef.appliedTo(ctx.definitions.IntType),
        None,
        None
      )
      val xArg = makeSymbol(
        NoSymbol,
        Names.termName("a"),
        Flags.LocalParam,
        ctx.definitions.IntType,
        None,
        None)
      val yArg = makeSymbol(
        NoSymbol,
        Names.termName("b"),
        Flags.LocalParam,
        ctx.definitions.IntType,
        None,
        None)
      val zArg = makeSymbol(
        NoSymbol,
        Names.termName("c"),
        Flags.LocalParam,
        ctx.definitions.IntType,
        None,
        None)
      val paramSymss = List(List(xArg, yArg), List(usingSuspendSymbol), List(zArg))
      val methodType = makeMethodType(paramSymss, ctx.definitions.IntType)
      val actualType = new ExampleTypeMap().apply(methodType)
      val expectedType = makeMethodType(
        paramSymss
          .insertAt(1, List(completionSymbol))
          .map(_.filterNot(_ == usingSuspendSymbol))
          .filterNot(_.isEmpty),
        ctx.definitions.IntType)
      assertTypeEquals(actualType, expectedType)
  }

}
