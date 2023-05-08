package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.NameOps.*
import dotty.tools.dotc.core.Scopes
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.MethodType
import dotty.tools.dotc.core.Constants.Constant

extension (sym: Symbol)
  def buildFrame(reservedVariables: List[Tree])(using ctx: Context): TypeDef =
    val anyType = ctx.definitions.AnyType
    val unitType = ctx.definitions.UnitType
    val continuationFrameClassNameStr = s"$$${sym.name}$$Frame"
    val continuationFrameClassIterator = Iterator.from(0).map(i => s"I$$$i")
    val continuationFrameClassName =
      continuationFrameClassNameStr.sliceToTypeName(0, continuationFrameClassNameStr.size)
    val continuationFrameSymbol = newCompleteClassSymbol(
      sym.owner,
      continuationFrameClassName,
      Flags.PrivateOrSynthetic,
      List(requiredClassRef(continuationImplFullName)),
      Scopes.newScope
    ).entered.asClass
    val bodyInputVars = reservedVariables.map { value =>
      val symbolNameStr = continuationFrameClassIterator.next()
      ValDef(
        newSymbol(
          continuationFrameSymbol,
          symbolNameStr.sliceToTermName(0, symbolNameStr.size),
          Flags.Mutable | Flags.Synthetic,
          anyType).entered.asTerm,
        Underscore(anyType)
      )
    }
    val eitherThrowableAnyNullSuspendedType =
      requiredClassRef("scala.util.Either").appliedTo(
        ctx.definitions.ThrowableType,
        anyType.? | requiredModuleRef(continuationFullName)
          .select(stateName.sliceToTermName(0, stateName.size))
          .select(suspendedName.sliceToTermName(0, suspendedName.size))
          .symbol
          .namedType
      )
    val frameResultType = requiredClassRef("scala.util.Either").appliedTo(
      List(
        ctx.definitions.ThrowableType,
        eitherThrowableAnyNullSuspendedType
      ))
    val $result = ValDef(
      newSymbol(
        continuationFrameSymbol,
        $resultName.sliceToTermName(0, $resultName.size),
        Flags.Accessor | Flags.Mutable,
        frameResultType
      ).entered.asTerm,
      Underscore(frameResultType)
    )
    val $label = ValDef(
      newSymbol(
        continuationFrameSymbol,
        $labelName.sliceToTermName(0, $labelName.size),
        Flags.Accessor | Flags.Mutable,
        ctx.definitions.IntType).entered.asTerm,
      Underscore(ctx.definitions.IntType)
    )

    val frameClassConstructor = DefDef(
      newConstructor(
        continuationFrameSymbol,
        Flags.Synthetic,
        List(completionParamName.sliceToTermName(0, completionParamName.size)),
        List(requiredClassRef(continuationFullName).appliedTo(anyType.?))
      ).entered.asTerm
    )
    val $completionRef = ref(
      frameClassConstructor
        .symbol
        .paramSymss
        .flatten
        .iterator
        .findSymbol(_.info.hasClassSymbol(requiredClass(continuationFullName))))

    ClassDefWithParents(
      continuationFrameSymbol,
      frameClassConstructor,
      List(
        New(requiredClassRef(continuationImplFullName))
          .select(nme.CONSTRUCTOR)
          .appliedToArgss(List(List(
            $completionRef,
            $completionRef.select(contextName.sliceToTermName(0, contextName.size)))))),
      bodyInputVars ++ bodyInputVars.map { value =>
        DefDef(
          newSymbol(
            continuationFrameSymbol,
            value.symbol.asTerm.name.asTermName.setterName,
            Flags.Accessor | Flags.Method,
            MethodType(List(value.symbol.asTerm.info), unitType)
          ).entered,
          params =>
            unitLiteral
        )
      } ++ List(
        $result,
        DefDef(
          newSymbol(
            continuationFrameSymbol,
            $result.symbol.asTerm.name.asTermName.setterName,
            Flags.Accessor | Flags.Method,
            MethodType(List($result.symbol.asTerm.info), unitType)
          ).entered,
          params =>
            unitLiteral
        ),
        $label,
        DefDef(
          newSymbol(
            continuationFrameSymbol,
            $label.symbol.asTerm.name.asTermName.setterName,
            Flags.Accessor | Flags.Method,
            MethodType(List($label.symbol.asTerm.info), unitType)
          ).entered,
          params =>
            unitLiteral
        ),
        DefDef(
          newSymbol(
            continuationFrameSymbol,
            invokeSuspendName.sliceToTermName(0, invokeSuspendName.size),
            Flags.Override | Flags.Protected | Flags.Method,
            MethodType(List(eitherThrowableAnyNullSuspendedType), anyType.?)
          ).entered.asTerm,
          params =>
            Block(
              List(
                Assign(
                  This(continuationFrameSymbol).select($result.name),
                  ref(params(0)(0).symbol.asTerm)
                ),
                Assign(
                  This(continuationFrameSymbol).select($label.name),
                  This(continuationFrameSymbol)
                    .select($label.name)
                    .select(
                      ctx
                        .definitions
                        .IntClass
                        .requiredMethod(nme.OR, List(ctx.definitions.IntType)))
                    .appliedTo(ref(requiredModuleRef("scala.Int")
                      .select(minValueName.sliceToTermName(0, minValueName.size))
                      .symbol))
                )
              ),
              ref(sym).appliedToArgss(
                sym
                  .paramSymss
                  .map(_.map { s =>
                    if (s.isType)
                      TypeTree(anyType)
                    else if (s.info.hasClassSymbol(requiredClass(continuationFullName)))
                      This(continuationFrameSymbol)
                        .select(nme.asInstanceOf_)
                        .appliedToType(requiredClassRef(continuationFullName).appliedTo(
                          sym.info.finalResultType))
                    else nullLiteral
                  }))
            )
        ),
        DefDef(
          newSymbol(
            continuationFrameSymbol,
            createName.sliceToTermName(0, createName.size),
            Flags.Method | Flags.Override,
            MethodType(
              List(
                anyType.?,
                requiredClassRef(continuationFullName).appliedTo(List(anyType.?))),
              requiredClassRef(continuationFullName).appliedTo(List(unitType))
            )
          ).entered.asTerm,
          params =>
            New(requiredClassRef(baseContinuationImplFullName), List(ref(params(0)(1).symbol)))
        )
      )
    )
  end buildFrame
