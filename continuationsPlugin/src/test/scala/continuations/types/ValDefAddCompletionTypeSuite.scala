package continuations
package types

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Flags.FlagSet
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Names.Name
import dotty.tools.dotc.core.Names.TypeName
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.TypeComparer
import dotty.tools.dotc.core.Types.*
import dotty.tools.dotc.util.Spans.Coord
import munit.FunSuite

class ValDefAddCompletionTypeSuite
    extends FunSuite,
      CompilerFixtures,
      TypeMapSuiteFixtures,
      TypeAssertions:

  methodTypeMakerAndCompiler.test(
    "it should remove suspend from a valdef type with additional context parameters and return a method type containing a continuation of the final result type") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val typeParams = List(TypeName(Names.termName("A")))
      val appliedType = AppliedType(
        requiredClassRef("scala.ContextFunction2"),
        List(
          ctx.definitions.StringType,
          requiredClassRef(suspendFullName),
          RefinedType(
            requiredClassRef("scala.PolyFunction"),
            Names.termName("apply"),
            PolyType(typeParams)(
              pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
              pt => {
                val typeParamRef = pt.newParamRef(0)
                val paramSymss = List(List(
                  requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef)))
                makeMethodType(paramSymss, ctx.definitions.IntType)
              }
            )
          )
        )
      )
      val actualType = ValDefAddCompletionType()(appliedType)
      val expectedType = makeMethodType(
        List(
          List(
            requiredClass(continuationFullName)
              .typeRef
              .appliedTo(AppliedType(
                requiredClassRef("scala.ContextFunction1"),
                List(
                  ctx.definitions.StringType,
                  RefinedType(
                    requiredClassRef("scala.PolyFunction"),
                    Names.termName("apply"),
                    PolyType(typeParams)(
                      pt =>
                        List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
                      pt => {
                        val typeParamRef = pt.newParamRef(0)
                        val paramSymss = List(List(requiredClassRef(
                          "scala.collection.immutable.List").appliedTo(typeParamRef)))
                        makeMethodType(paramSymss, ctx.definitions.IntType)
                      }
                    )
                  )
                )
              )))),
        AppliedType(
          requiredClassRef("scala.ContextFunction1"),
          List(
            ctx.definitions.StringType,
            RefinedType(
              requiredClassRef("scala.PolyFunction"),
              Names.termName("apply"),
              PolyType(typeParams)(
                pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
                pt => {
                  val typeParamRef = pt.newParamRef(0)
                  val paramSymss = List(List(requiredClassRef("scala.collection.immutable.List")
                    .appliedTo(typeParamRef)))
                  makeMethodType(paramSymss, ctx.definitions.IntType)
                }
              )
            )
          )
        )
      )
      assertTypeEquals(actualType, expectedType)
  }
  methodTypeMakerAndCompiler.test(
    "it should remove suspend from a valdef type and return a method type containing a continuation of the final result type") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val typeParams = List(TypeName(Names.termName("A")))
      val appliedType = AppliedType(
        requiredClassRef("scala.ContextFunction1"),
        List(
          requiredClassRef(suspendFullName),
          RefinedType(
            requiredClassRef("scala.PolyFunction"),
            Names.termName("apply"),
            PolyType(typeParams)(
              pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
              pt => {
                val typeParamRef = pt.newParamRef(0)
                val paramSymss = List(List(
                  requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef)))
                makeMethodType(paramSymss, ctx.definitions.IntType)
              }
            )
          )
        )
      )
      val actualType = ValDefAddCompletionType()(appliedType)
      val expectedType = makeMethodType(
        List(
          List(
            requiredClass(continuationFullName)
              .typeRef
              .appliedTo(RefinedType(
                requiredClassRef("scala.PolyFunction"),
                Names.termName("apply"),
                PolyType(typeParams)(
                  pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
                  pt => {
                    val typeParamRef = pt.newParamRef(0)
                    val paramSymss = List(List(requiredClassRef(
                      "scala.collection.immutable.List").appliedTo(typeParamRef)))
                    makeMethodType(paramSymss, ctx.definitions.IntType)
                  }
                )
              )))),
        RefinedType(
          requiredClassRef("scala.PolyFunction"),
          Names.termName("apply"),
          PolyType(typeParams)(
            pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
            pt => {
              val typeParamRef = pt.newParamRef(0)
              val paramSymss = List(
                List(
                  requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef)))
              makeMethodType(paramSymss, ctx.definitions.IntType)
            }
          )
        )
      )
      assertTypeEquals(actualType, expectedType)
  }

  methodTypeMakerAndCompiler.test(
    "it should remove suspend from a valdef declaring a function type and return a method containing a continuation of the function type") {
    case (makeMethod, given Context) =>
      val ctx = summon[Context]
      val appliedType = requiredClassRef("scala.ContextFunction1").appliedTo(
        List(
          requiredClassRef(suspendFullName),
          requiredClassRef("scala.Function1")
            .appliedTo(ctx.definitions.IntType, ctx.definitions.IntType)))
      val actualType = ValDefAddCompletionType()(appliedType)
      val expectedType = makeMethod(
        List(
          List(requiredClassRef(continuationFullName).appliedTo(requiredClassRef(
            "scala.Function1").appliedTo(ctx.definitions.IntType, ctx.definitions.IntType)))),
        requiredClassRef("scala.Function1")
          .appliedTo(ctx.definitions.IntType, ctx.definitions.IntType)
      )
      assertTypeEquals(actualType, expectedType)

  }

  methodTypeMakerAndCompiler.test(
    "it should remove suspend from a valdef declaring a normal return type and return a method containing a continuation of the normal type") {
    case (makeMethod, given Context) =>
      val ctx = summon[Context]
      val appliedType = requiredClassRef("scala.ContextFunction1").appliedTo(
        List(requiredClassRef(suspendFullName), ctx.definitions.IntType))
      val actualType = ValDefAddCompletionType()(appliedType)
      val expectedType = makeMethod(
        List(List(requiredClassRef(continuationFullName).appliedTo(ctx.definitions.IntType))),
        ctx.definitions.IntType)
      assertTypeEquals(actualType, expectedType)

  }
end ValDefAddCompletionTypeSuite
