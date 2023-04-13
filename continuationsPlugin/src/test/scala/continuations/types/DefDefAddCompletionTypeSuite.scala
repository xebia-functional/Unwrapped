package continuations
package types

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Names.TypeName
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.TypeComparer
import dotty.tools.dotc.core.Types.*
import munit.FunSuite

class DefDefAddCompletionTypeSuite
    extends FunSuite,
      CompilerFixtures,
      TypeMapSuiteFixtures,
      TypeAssertions:

  methodTypeMakerAndCompiler.test(
    "it should remove using Suspend from a method's type and insert a continuation of the final result type as a new type list at the index prior to the Suspend type.") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val usingSuspendType = requiredClass(suspendFullName).typeRef
      val completionType =
        requiredClass(continuationFullName).typeRef.appliedTo(ctx.definitions.IntType)
      val xArg = ctx.definitions.IntType
      val yArg = ctx.definitions.IntType
      val zArg = ctx.definitions.IntType
      val paramSymss = List(List(xArg, yArg), List(usingSuspendType), List(zArg))
      val methodType = makeMethodType(paramSymss, ctx.definitions.IntType)
      val actualType = DefDefAddCompletionType()(methodType)
      val expectedType = makeMethodType(
        paramSymss
          .insertAt(1, List(completionType))
          .map(_.filterNot(TypeComparer.isSameType(_, usingSuspendType)))
          .filterNot(_.isEmpty),
        ctx.definitions.IntType
      )
      assertTypeEquals(actualType, expectedType)
  }

  methodTypeMakerAndCompiler.test(
    "it should remove using suspend froma a method's type and insert a continuaition of the final result type as a new type list when the method has no other arguments") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val usingSuspendType = requiredClass(suspendFullName).typeRef
      val completionType =
        requiredClass(continuationFullName).typeRef.appliedTo(ctx.definitions.IntType)
      val paramSymss = List(List(usingSuspendType))
      val methodType = makeMethodType(paramSymss, ctx.definitions.IntType)
      val actualType = DefDefAddCompletionType()(methodType)
      val expectedType = makeMethodType(
        paramSymss
          .insertAt(1, List(completionType))
          .map(_.filterNot(TypeComparer.isSameType(_, usingSuspendType)))
          .filterNot(_.isEmpty),
        ctx.definitions.IntType
      )
      assertTypeEquals(actualType, expectedType)
  }
  methodTypeMakerAndCompiler.test(
    "it should remove using suspend from a method's type and insert a continuation of the final result type as a new type list when the method has a generic type argument") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val usingSuspendType = requiredClassRef(suspendFullName)
      val typeParams = List(TypeName(Names.termName("A")))
      // due to type comparison issues, we have to compare the polytypes applied to some type for equality
      val polyMethodType = PolyType(typeParams)(
        pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
        pt => {
          val typeParamRef = pt.newParamRef(0)
          val x = typeParamRef
          val paramSymss = List(List(x), List(usingSuspendType))
          makeMethodType(paramSymss, typeParamRef)
        }
      ).appliedTo(ctx.definitions.IntType)
      val actualType = DefDefAddCompletionType()(polyMethodType)
      val expectedType = PolyType(typeParams)(
        pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
        pt => {
          val typeParamRef = pt.newParamRef(0)
          val x =
            typeParamRef
          val completionSymbol =
            requiredClass(continuationFullName).typeRef.appliedTo(typeParamRef)
          val paramSymss = List(List(x), List(completionSymbol))
          makeMethodType(paramSymss, typeParamRef)
        }
      ).appliedTo(ctx.definitions.IntType)
      assertTypeEquals(actualType, expectedType)
  }
  methodTypeMakerAndCompiler.test(
    "it should remove suspend from a method's type and insert a continuation of the final result type as a new type list when the method returns a context function of Suspend") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val completionSymbol = requiredClass(continuationFullName)
        .typeRef
        .appliedTo(RefinedType(
          requiredClassRef("scala.PolyFunction"),
          Names.termName("apply"),
          PolyType(List(TypeName(Names.termName("A"))))(
            pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
            pt => {
              val typeParamRef = pt.newParamRef(0)
              val x =
                requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef)
              val paramTypess = List(List(x))
              makeMethodType(paramTypess, ctx.definitions.IntType)
            }
          )
        ))

      val expressionType = ExprType(
        AppliedType(
          requiredClassRef("scala.ContextFunction1"),
          List(
            requiredClassRef(suspendFullName),
            RefinedType(
              requiredClassRef("scala.PolyFunction"),
              Names.termName("apply"),
              PolyType(List(TypeName(Names.termName("A"))))(
                pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
                pt => {
                  val typeParamRef = pt.newParamRef(0)
                  val x =
                    requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef)
                  val paramTypess = List(List(x))
                  makeMethodType(paramTypess, ctx.definitions.IntType)
                }
              )
            )
          )
        ))
      val actualType = DefDefAddCompletionType()(expressionType)
      val expectedType = makeMethodType(
        List(List(completionSymbol)),
        RefinedType(
          requiredClassRef("scala.PolyFunction"),
          Names.termName("apply"),
          PolyType(List(TypeName(Names.termName("A"))))(
            pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
            pt => {
              val typeParamRef = pt.newParamRef(0)
              val x =
                requiredClassRef("scala.collection.immutable.List").appliedTo(typeParamRef)
              val paramTypess = List(List(x))
              makeMethodType(paramTypess, ctx.definitions.IntType)
            }
          )
        )
      )
      assertTypeEquals(actualType, expectedType)
  }

  methodTypeMakerAndCompiler.test(
    "it should remove suspend from a method's type and insert a continuation of the final result type as a new type list when the method returns a context function parameter that is not suspend") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val typeParams = List(TypeName(Names.termName("A")))
      val expressionType = ExprType(
        AppliedType(
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
                  val paramSymss = List(List(requiredClassRef("scala.collection.immutable.List")
                    .appliedTo(typeParamRef)))
                  makeMethodType(paramSymss, ctx.definitions.IntType)
                }
              )
            )
          )
        ))
      val actualType = DefDefAddCompletionType()(expressionType)
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
    "it should remove suspend from a contextual method's type and insert a continuation of the final result type as a new type list when the method returns a generic expression type") {
    case (makeMethodType, given Context) =>
      val ctx = summon[Context]
      val contextualMethodType = ContextualMethodType(
        List(requiredClassRef(suspendFullName)),
        RefinedType(
          requiredClassRef("scala.PolyFunction"),
          Names.termName("apply"),
          PolyType(List(Names.typeName("A")))(
            pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
            pt =>
              makeMethodType(
                List(
                  List(
                    AppliedType(
                      requiredClassRef("scala.collection.immutable.List"),
                      List(pt.newParamRef(0))))),
                ctx.definitions.IntType)
          )
        )
      )
      val actualType = DefDefAddCompletionType()(contextualMethodType)
      val expectedType = makeMethodType(
        List(
          List(requiredClassRef(continuationFullName).appliedTo(RefinedType(
            requiredClassRef("scala.PolyFunction"),
            Names.termName("apply"),
            PolyType(List(Names.typeName("A")))(
              pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
              pt =>
                makeMethodType(
                  List(List(AppliedType(
                    requiredClassRef("scala.collection.immutable.List"),
                    List(pt.newParamRef(0))))),
                  ctx.definitions.IntType)
            )
          )))),
        RefinedType(
          requiredClassRef("scala.PolyFunction"),
          Names.termName("apply"),
          PolyType(List(Names.typeName("A")))(
            pt => List(TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
            pt =>
              makeMethodType(
                List(
                  List(
                    AppliedType(
                      requiredClassRef("scala.collection.immutable.List"),
                      List(pt.newParamRef(0))))),
                ctx.definitions.IntType)
          )
        )
      )
      assertTypeEquals(actualType, expectedType)
  }

end DefDefAddCompletionTypeSuite
