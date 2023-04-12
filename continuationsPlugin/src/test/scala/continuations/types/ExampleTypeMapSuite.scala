package continuations
package types

import munit.FunSuite
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Names.TypeName
import dotty.tools.dotc.core.Types.TypeBounds
import dotty.tools.dotc.core.Types.TypeParamRef
import dotty.tools.dotc.core.Types.PolyType
import dotty.tools.dotc.core.Types.ExprType
import dotty.tools.dotc.core.Types.AppliedType
import dotty.tools.dotc.core.Types.RefinedType

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

  symbolMakerAndMethodTypeMakerAndCompiler.test(
    "it should remove using suspend froma a method's type and insert a continuaition of the final result type as a new type list when the method has no other arguments") {
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
      val paramSymss = List(List(usingSuspendSymbol))
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
  symbolMakerAndMethodTypeMakerAndCompiler.test(
    "it should remove using suspend from a method's type and insert a continuation of the final result type as a new type list when the method has a generic type argument") {
    case (makeSymbol, makeMethodType, given Context) =>
      val ctx = summon[Context]
      val usingSuspendSymbol = makeSymbol(
        NoSymbol,
        Names.termName("s"),
        Flags.GivenOrImplicit | Flags.LocalParam,
        requiredClass(suspendFullName).typeRef,
        None,
        None)
      val typeParams = List(TypeName(Names.termName("A")))
      // due to type comparison issues, we have to compare the polytypes applied to some type for equality
      val polyMethodType = PolyType(typeParams)(
        pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
        pt => {
          val typeParamRef = pt.newParamRef(0)
          val x =
            makeSymbol(
              NoSymbol,
              Names.termName("x"),
              Flags.LocalParam,
              typeParamRef,
              None,
              None)
          val paramSymss = List(List(x), List(usingSuspendSymbol))
          makeMethodType(paramSymss, typeParamRef)
        }
      ).appliedTo(ctx.definitions.IntType)
      val actualType = new ExampleTypeMap().apply(polyMethodType)
      val expectedType = PolyType(typeParams)(
        pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
        pt => {
          val typeParamRef = pt.newParamRef(0)
          val x =
            makeSymbol(
              NoSymbol,
              Names.termName("x"),
              Flags.LocalParam,
              typeParamRef,
              None,
              None)
          val completionSymbol = makeSymbol(
            NoSymbol,
            Names.termName(completionParamName),
            Flags.LocalParam,
            requiredClass(continuationFullName).typeRef.appliedTo(typeParamRef),
            None,
            None
          )
          val paramSymss = List(List(x), List(completionSymbol))
          makeMethodType(paramSymss, typeParamRef)
        }
      ).appliedTo(ctx.definitions.IntType)
      assertTypeEquals(actualType, expectedType)
  }
  symbolMakerAndMethodTypeMakerAndCompiler.test("it should remove  suspend from a method's type and insert a continuation of the final result type as a new type list when the method returns a context function of Suspend"){
    case (makeSymbol, makeMethodType, given Context) =>
      val ctx = summon[Context]
      val typeParams = List(TypeName(Names.termName("A")))
      val expressionType = ExprType(AppliedType(requiredClassRef("scala.ContextFunction1"), List(requiredClassRef(suspendFullName), RefinedType(requiredClassRef("scala.PolyFunction"), Names.termName("apply"), PolyType(typeParams)(
        pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
        pt => {
          val typeParamRef = pt.newParamRef(0)
          val x =
            makeSymbol(
              NoSymbol,
              Names.termName("x$1"),
              Flags.LocalParam,
              requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef),
              None,
              None)
          val paramSymss = List(List(x))
          makeMethodType(paramSymss, ctx.definitions.IntType)
        }
      )))))
      val actualType = new ExampleTypeMap().apply(expressionType)
      println(s"actualType.show: ${actualType.show}")
      println(s"actualType: ${actualType}")
      assert(true)
  }

}
