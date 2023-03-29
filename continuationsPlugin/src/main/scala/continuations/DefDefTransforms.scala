package continuations

import continuations.DefDefTransforms.*
import continuations.Types.flattenTypes
import continuations.jvm.internal.ContinuationImpl
import dotty.tools.dotc.ast.{tpd, TreeTypeMap, Trees}
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Annotations.Annotation
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Symbols, Types}
import dotty.tools.dotc.core.NullOpsDecorator.stripNull
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.{termName, typeName}
import dotty.tools.dotc.core.NameOps.*
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType, PolyType, RefinedType, Type}
import dotty.tools.dotc.report
import dotty.tools.dotc.transform.ContextFunctionResults

import scala.annotation.tailrec
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import dotty.tools.dotc.core.Types.TypeMap
import dotty.tools.dotc.core.Types.NamedType
import dotty.tools.dotc.core.Types.LambdaParam
import dotty.tools.dotc.core.Types.ContextualMethodType
import dotty.tools.dotc.core.Types.NoType
import dotty.tools.dotc.util.Spans
import dotty.tools.dotc.core.Types.AppliedType
import dotty.tools.dotc.core.Types.TermRef
import dotty.tools.dotc.core.Types.ThisType
import dotty.tools.dotc.core.Types.CachedThisType
import dotty.tools.dotc.core.Types.TypeRef
import dotty.tools.dotc.core.Types.SuperType
import dotty.tools.dotc.core.Types.ConstantType
import dotty.tools.dotc.core.Types.TermParamRef
import dotty.tools.dotc.core.Types.SkolemType
import dotty.tools.dotc.core.Types.RecType
import dotty.tools.dotc.core.Types.AnnotatedType
import dotty.tools.dotc.core.Types.CachedAnnotatedType
import dotty.tools.dotc.core.Types.TypeVar

object DefDefTransforms extends TreesChecks:

  final class TransformedMethodTypeMap(completionIndex: Int, resultTpe: Type)(using Context)
      extends TypeMap {
    override def apply(tp: Type): Type = tp match {
      case t: Types.TypeRef if t.symbol.info.hasClassSymbol(requiredClass(suspendFullName)) =>
        NoType
      case t @ MethodType(termNames) =>
        val withoutSuspend =
          t.paramInfoss.map(_.map(apply).filterNot(ttpe => ttpe == NoType)).filterNot(_.isEmpty)
        val completionType = requiredPackage(continuationPackageName)
          .requiredType(continuationClassName)
          .typeRef
          .appliedTo(resultTpe)

        val newParamInfoss = withoutSuspend.insertAt(completionIndex, List(completionType))
        newParamInfoss
          .reverse
          .foldLeft(Option.empty[MethodType]) {
            case (None, args) =>
              Some(MethodType.apply(args, resultTpe))
            case (Some(mt), args) =>
              Some(MethodType.apply(args, mt))
          }
          .get
      case t @ PolyType(lambdaParams, tpe) =>
        PolyType(t.paramNames)(pt => t.paramInfos, pt => apply(tpe))
      case t =>
        t
    }
  }

  def isContinuation(t: tpd.Tree)(using Context): Boolean =
    t.symbol.info.hasClassSymbol(requiredClass(continuationFullName))
  def isSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.info.hasClassSymbol(requiredClass(suspendFullName))

  def transformParamsRemovingSuspend(paramss: List[tpd.ParamClause])(
      using Context): List[tpd.ParamClause] =
    TreeTypeMap(
      treeMap = {
        case v: tpd.ValDef if isSuspend(v) =>
          tpd.EmptyTree
        case p => p
      }
    ).transformParamss(paramss)
      .map(_.filterNot(_ == tpd.EmptyTree))
      .filterNot(_.isEmpty)
      .asInstanceOf[List[tpd.ParamClause]]

  def addCompletionParam(valOrDefDef: tpd.ValOrDefDef)(using Context): tpd.DefDef = {
    valOrDefDef match {
      case tree: tpd.DefDef if(!tree.paramss.exists(_.exists(_.symbol.info.hasClassSymbol(requiredClass(continuationFullName))))) =>
        val maybeCompletionIndex =
          tree.paramss.indexWhere(_.exists(_.symbol.isOneOf(GivenOrImplicit)))
        val completionIndex =
          if (maybeCompletionIndex == -1) tree.paramss.size - 1 else maybeCompletionIndex
        val newTreeType =
          TransformedMethodTypeMap(completionIndex, tree.tpt.tpe).apply(tree.symbol.info)
        val newTreeSymbol = tree.symbol.asTerm.copy(info = newTreeType).entered.asTerm
        val completionSymbol = newSymbol(
          newTreeSymbol,
          Names.termName(completionParamName),
          Flags.LocalParam | Flags.SyntheticParam,
          requiredPackage(continuationPackageName)
            .requiredType(continuationClassName)
            .typeRef
            .appliedTo(tree.tpt.tpe)
        ).entered
        val completionValDef = tpd.ValDef(completionSymbol.asTerm, tpd.EmptyTree)

        val treeWithCompletion =
          cpy.DefDef(tree)(paramss = transformParamsRemovingSuspend(
            tree.paramss.insertAt(completionIndex, List(completionValDef))))

        val ownershipChanger = TreeTypeMap(
          substFrom = List(tree.symbol),
          substTo = List(newTreeSymbol),
          oldOwners = List(tree.symbol),
          newOwners = List(newTreeSymbol))
        val (ttm, treeWithChangedOwnerss) =
          ownershipChanger.transformDefs(List(treeWithCompletion))
        treeWithChangedOwnerss.head
      case tree: tpd.DefDef => tree
      case tree: tpd.ValDef =>
        println(s"ValDef")
        val returnType = flattenTypes(tree.symbol.info).last
        println(s"returnType: ${returnType}")
        val typeWithoutSuspend = removeSuspend(tree.symbol.info, Some(returnType))
        println(s"typeWithoutSuspend: ${typeWithoutSuspend}")
        val completionType = requiredPackage(continuationPackageName)
          .requiredType(continuationClassName)
          .typeRef
          .appliedTo(returnType)
        println(s"completionType: ${completionType}")
        val newMethodType = MethodType.apply(List(Names.termName(completionParamName)))(
          mt => List(completionType),
          mt => typeWithoutSuspend)
        val newTreeSymbol = tree
          .symbol
          .asTerm
          .copy(info = newMethodType, flags = tree.symbol.flags | Flags.Method)
          .entered
          .asTerm
        println(s"newMethodType: $newMethodType")
        println(s"oldTree.rhs: ${tree.rhs.show}")
        val newTree = tpd.DefDef(newTreeSymbol, tree.forceIfLazy)
        println(s"newTree: ${newTree.show}")
        newTree
    }
  }

  def transformSuspendContinuation(tree: tpd.ValOrDefDef)(using Context): tpd.Tree =
    tree match {
      case _
          if ReturnsContextFunctionWithSuspendType(tree) && !tree.symbol.isAnonymousFunction =>
        report.logWith(s"new tree:")(transformContinuationWithSuspend(tree))
      case _ if HasSuspendParameter(tree) && !tree.symbol.isAnonymousFunction =>
        report.logWith(s"new tree:")(transformContinuationWithSuspend(tree))
      case _ => report.logWith(s"oldTree:")(tree)
    }

  @tailrec
  def flattenBlock(b: tpd.Tree)(using Context): tpd.Tree =
    b match {
      case bb @ Trees.Block(Nil, _: tpd.Closure) => bb
      case Trees.Block(Nil, bb @ tpd.Block(_, _)) => flattenBlock(bb)
      case b => b
    }

  private class BlockFlattener extends TreeMap {
    override def transform(tree: tpd.Tree)(using Context): tpd.Tree =
      tree match {
        case bb @ Trees.Block(Nil, Trees.Block(Nil, Trees.Closure(_, _, _))) =>
          super.transform(bb)
        case Trees.Block(Nil, bb) =>
          super.transform(bb)
        case t => super.transform(t)
      }
  }

  private def transformContinuationWithSuspend(tree: tpd.ValOrDefDef)(using Context): tpd.Tree =
    def fetchSuspensions =
      SuspensionPoints
        .unapplySeq(report.logWith("tree has no suspension points:")(tree))
        .toList
        .flatten

    def transformSuspensionsSuspendingStateMachine(
        suspensionPoints: List[tpd.Tree],
        suspensionInReturnedValue: Boolean)(using ctx: Context) =
      if suspensionPoints.isEmpty then tree
      else
        val transformedTree =
          transformSuspensionsSuspendingStateMachineAux(
            addCompletionParam(tree),
            suspensionPoints,
            suspensionInReturnedValue)
        report.logWith("state machine and new defdef:")(transformedTree)

    tree match
      case _ if HasSuspensionNotInReturnedValue(tree) =>
        transformSuspensionsSuspendingStateMachine(fetchSuspensions, false)
      case _ if CallsSuspendContinuation(tree) =>
        fetchSuspensions match
          case suspensionPoint :: Nil if !suspensionPoint.isInstanceOf[tpd.ValDef] =>
            transformSuspendOneContinuationResume(tree, suspensionPoint)
          case suspensionPoints =>
            transformSuspensionsSuspendingStateMachine(suspensionPoints, true)
      case vd: tpd.DefDef if BodyHasSuspensionPoint(vd) =>
        // any suspension that still needs a transformation
        tree match
          case t: tpd.DefDef => cpy.DefDef(t)()
          case t: tpd.ValDef => cpy.ValDef(t)()
      case _ => transformNonSuspending(tree)

  private def transformSuspendContinuationBody(
      callSuspensionPoint: tpd.Tree,
      safeContinuationSym: Symbol)(using Context): tpd.Tree = {
    println(s"callSuspensionPoint: ${callSuspensionPoint.show}")
    val resumeMethod = ref(safeContinuationSym).select(termName(resumeMethodName))
    val raiseMethod = ref(safeContinuationSym).select(termName(raiseMethodName))

    def isAnonContFunction(t: tpd.Tree): Boolean =
      t match {
        case t @ Trees.DefDef(_, _, _, _) =>
          t.symbol.exists &&
            t.symbol.isAnonymousFunction &&
            t.symbol.paramSymss.flatten.exists(hasContinuationClass)
        case tree @ Trees.Select(_, name) =>
          belongsToContinuation(tree.symbol) &&
            (name.show == resumeMethodName || name.show == raiseMethodName)
        case _ => false
      }

    val anonymousContinuationFunctions = callSuspensionPoint.filterSubTrees(isAnonContFunction)

    val continuationReferences = anonymousContinuationFunctions.map(_.symbol.owner)

    val safeContinuationRefSymbols =
      List.fill(anonymousContinuationFunctions.size)(safeContinuationSym)
    val safeContinuationRefOwners = safeContinuationRefSymbols.map(_.owner)

    val hypothesis = new TreeTypeMap(
      treeMap = {
        case t: tpd.DefDef if t.paramss.exists(_.exists(p => hasContinuationClass(p.symbol))) =>
          t.rhs
        case Trees.Apply(fun, args) if fun.symbol.name.show == shiftName =>
          if (args.isEmpty) {
            tpd.EmptyTree
          } else if (args.size == 1) {
            tpd.Block(List.empty, args.last)
          } else tpd.Block(args.dropRight(1), args.last)
        case Trees.Block(stats, Trees.Closure(_, m, _)) if m.existsSubTree { t =>
              anonymousContinuationFunctions.exists(anon => t.symbol.containsSym(anon.symbol))
            } =>
          if (stats.isEmpty) {
            tpd.EmptyTree
          } else if (stats.size == 1) {
            tpd.Block(List.empty, stats.last)
          } else {
            val flattened = Trees.flatten(stats)
            tpd.Block(flattened.dropRight(1), flattened.last)
          }
        case tree @ Trees.Select(_, name)
            if belongsToContinuation(tree.symbol) && name.show == resumeMethodName =>
          resumeMethod
        case tree @ Trees.Select(_, name)
            if belongsToContinuation(tree.symbol) && name.show == raiseMethodName =>
          raiseMethod
        case t =>
          t
      },
      oldOwners = continuationReferences.map(_.owner),
      newOwners = safeContinuationRefOwners,
      substFrom = continuationReferences,
      substTo = safeContinuationRefSymbols)(callSuspensionPoint)

    val leftoverSymbols = hypothesis
      .filterSubTrees { t =>
        val owner = t.symbol.maybeOwner
        owner.isAnonymousFunction &&
        owner.paramSymss.flatten.exists(hasContinuationClass)
      }
      .map(_.symbol.maybeOwner)

    val leftoverReplacementSymbols = List.fill(leftoverSymbols.size)(safeContinuationSym)
    val leftoverReplocementSymbolOvers = leftoverReplacementSymbols.map(_.owner)

    val newBlock = new BlockFlattener().transform(
      new TreeTypeMap(
        oldOwners = leftoverSymbols,
        newOwners = leftoverReplocementSymbolOvers,
        substFrom = leftoverSymbols,
        substTo = leftoverReplacementSymbols
      )(hypothesis))
    println(s"newBlock: ${newBlock.show}")
    newBlock
  }

  private def createTransformedMethodSymbol(
      parent: Symbol,
      transformedMethodParams: List[tpd.ParamClause],
      returnType: Type,
      owner: Option[Symbol] = None)(using Context) =
    newSymbol(
      owner.getOrElse(parent.owner),
      parent.name.asTermName,
      parent.flags | Flags.Method, {
        transformedMethodParams
          .reverse
          .foldLeft(Option.empty[MethodType]) { (acc, paramClause: tpd.ParamClause) =>
            acc
              .map { mt =>
                val newMt = MethodType.apply(
                  paramClause.map {
                    case t: tpd.TypeDef => t.tpe
                    case v => v.symbol.info
                  },
                  mt)
                newMt
              }
              .orElse(Some(MethodType.apply(
                paramClause.map {
                  case t: tpd.TypeDef => t.tpe
                  case v => v.symbol.info
                },
                returnType)))
          }
          .getOrElse(MethodType.fromSymbols(List.empty, returnType))
      },
      parent.privateWithin,
      parent.coord
    ).entered

  private def hasSuspendClass(s: Symbol)(using ctx: Context): Boolean =
    s.info.hasClassSymbol(requiredClass(suspendFullName))

  private def hasContinuationClass(s: Symbol)(using ctx: Context): Boolean =
    s.info.hasClassSymbol(requiredClass(continuationFullName))

  private def isImplicit(sym: Symbol)(using Context): Boolean =
    sym.flags.isOneOf(Flags.GivenOrImplicit)

  private def belongsToContinuation(symbol: Symbol)(using Context): Boolean =
    symbol.owner.name.show == continuationClassName

  private def matchesNameCoord(v: Symbol, tree: tpd.Tree)(using ctx: Context): Boolean =
    v.name == tree.symbol.name && v.coord == tree.symbol.coord

  private def blockOf(stats: List[tpd.Tree])(using Context): tpd.Tree =
    stats match {
      case Nil => tpd.unitLiteral
      case x :: Nil => x
      case xs => tpd.Block(xs.dropRight(1), xs.last)
    }

  private def fieldMatchesParam(field: Symbol, param: Symbol)(using Context): Boolean =
    field.name.show.dropRight(3) == param.name.show

  private def removeSuspend(typ: Type, returnValue: Option[Type] = None)(using Context): Type =
    val types = flattenTypes(typ)

    types.foldRight(returnValue.getOrElse(types.last)) {
      case (defn.FunctionOf(args, _, ic, ie), inner) =>
        val argsWithoutSuspend =
          args.filterNot(_.hasClassSymbol(requiredClass(suspendFullName)))

        if (argsWithoutSuspend.nonEmpty)
          defn.FunctionOf(
            args = argsWithoutSuspend,
            resultType = inner,
            isContextual = ic,
            isErased = ie)
        else
          inner
      case (RefinedType(p, n, PolyType(ptp, mt @ MethodType(mtp))), inner) =>
        RefinedType(
          parent = p,
          name = n,
          info = PolyType.fromParams(
            params = ptp,
            resultType = MethodType(paramNames = mtp)(
              paramInfosExp = _ => mt.paramInfos,
              resultTypeExp = _ => inner)))
      case (_, inner) =>
        inner
    }

  private def removeSuspendReturnAny(typ: Type)(using Context): Type =
    removeSuspend(typ, Some(ref(defn.AnyType).tpe))

  private def getBodyContextFunctionOwner(tree: tpd.ValOrDefDef)(
      using Context): (tpd.Tree, Option[Symbol]) =
    if (ReturnsContextFunctionWithSuspendType(tree))
      val (rhs, contextFunctionOwner) = tree.rhs match
        case tpd.Block(List(d @ tpd.DefDef(_, _, _, _)), _) if d.symbol.isAnonymousFunction =>
          (d.rhs, Option(d.symbol))
        case rhs => (rhs, Option.empty)

      (rhs, contextFunctionOwner)
    else (tree.rhs, Option.empty)

  private def deleteOldSymbol(sym: Symbol)(using Context): Unit =
    sym.enclosingClass.asClass.delete(sym)

  private def transformSuspendOneContinuationResume(
      tree: tpd.ValOrDefDef,
      suspensionPoint: tpd.Tree)(using Context): tpd.DefDef = {

    val continuationTraitSym: ClassSymbol = requiredClass(continuationFullName)
    val continuationObjectSym: Symbol = continuationTraitSym.companionModule

    val treeWithCompletion = addCompletionParam(tree)

    val (rhs, contextFunctionOwner) =
      getBodyContextFunctionOwner(tree)

    val transformedMethodCompletionParam =
      ref(
        treeWithCompletion
          .paramss
          .flatten
          .find(_.symbol.info.hasClassSymbol(requiredClass(continuationFullName)))
          .get
          .symbol)

    val returnType = treeWithCompletion.tpt.tpe

    val continuation1: tpd.ValDef =
      val continuationTyped: Type =
        continuationTraitSym.typeRef.appliedTo(returnType)
      tpd.ValDef(
        sym = newSymbol(
          treeWithCompletion.symbol,
          termName("continuation1"),
          Flags.Local,
          continuationTyped).entered,
        rhs = transformedMethodCompletionParam
      )

    /*
     ```
     val safeContinuation: continuations.SafeContinuation[Int] =
       new continuations.SafeContinuation[Int](
         continuations.intrinsics.IntrinsicsJvm$package.intercepted[Int]($continuation)(),
         continuations.Continuation.State.Undecided
       )
     ```
     */
    val safeContinuation: tpd.ValDef = {
      val constructor =
        ref(requiredModule("continuations.SafeContinuation"))
          .select(termName("init"))
          .appliedToType(returnType)
          .appliedTo(ref(continuation1.symbol))

      val sym: TermSymbol = newSymbol(
        treeWithCompletion.symbol,
        termName("safeContinuation"),
        Flags.Local,
        constructor.tpe).entered

      tpd.ValDef(sym, constructor)
    }

    val callSuspensionPoint: tpd.Tree =
      suspensionPoint match
        case tpd.Inlined(call, _, _) =>
          new TreeTypeMap(
            oldOwners = List(tree.symbol),
            newOwners = List(treeWithCompletion.symbol),
            substFrom = List(tree.symbol),
            substTo = List(treeWithCompletion.symbol)
          ).transform(call)
        case _ => tpd.EmptyTree

    val continuationBlock = blockOf(
      List(
        continuation1,
        safeContinuation,
        transformSuspendContinuationBody(callSuspensionPoint, safeContinuation.symbol),
        ref(safeContinuation.symbol).select(termName("getOrThrow")).appliedToNone
      ))
    println(s"continuationBlock: ${continuationBlock.show}")

    val transformedMethodParamSymbols: List[Symbol] =
      treeWithCompletion
        .paramss
        .flatMap(_.filterNot(p => hasContinuationClass(p.symbol) || p.symbol.isType))
        .map(_.symbol)

    println(s"transformedMethodParamSymbols: ${transformedMethodParamSymbols}")
    val oldMethodParamSymbols: List[Symbol] =
      tree.symbol.paramSymss.flatMap(_.filterNot(s => hasSuspendClass(s) || s.isTypeParam))

    val substituteContinuation = new TreeTypeMap(
      treeMap = tree => if treeCallsSuspend(tree) then continuationBlock else tree,
      substFrom = List(tree.symbol) ++ oldMethodParamSymbols ++ contextFunctionOwner.toList,
      substTo = List(treeWithCompletion.symbol) ++ transformedMethodParamSymbols ++
        List(treeWithCompletion.symbol),
      oldOwners = List(tree.symbol) ++ contextFunctionOwner.toList,
      newOwners = List(treeWithCompletion.symbol) ++ treeWithCompletion
        .paramss
        .map(_.map(_.symbol.owner))
        .flatten
    )

    val newRHS = substituteContinuation.transform(rhs)
    println(s"newRHS: ${newRHS.show}")
    cpy.DefDef(treeWithCompletion)(rhs = newRHS)
  }

  private def transformNonSuspending(tree: tpd.ValOrDefDef)(using ctx: Context): tpd.Tree =
    val treeWithCompletion = addCompletionParam(tree)

    ContextFunctionResults.annotateContextResults(treeWithCompletion)
    treeWithCompletion

  /* The representation for the following code:
   ```
     case _: Int => throw new IllegalArgumentException("call to 'resume' before 'invoke' with coroutine")
   ```
   */
  private def wrongStateCase(using Context): tpd.CaseDef = {
    val IllegalArgumentExceptionClass_stringConstructor: TermSymbol =
      defn
        .IllegalArgumentExceptionClass
        .info
        .member(nme.CONSTRUCTOR)
        .suchThat(_.info.firstParamTypes match {
          case List(pt) =>
            pt.stripNull.isRef(defn.StringClass)
          case _ => false
        })
        .symbol
        .asTerm

    tpd.CaseDef(
      tpd.Underscore(defn.IntType),
      tpd.EmptyTree,
      tpd.Throw(
        tpd.New(
          defn.IllegalArgumentExceptionType,
          IllegalArgumentExceptionClass_stringConstructor,
          List(tpd.Literal(Constant("call to 'resume' before 'invoke' with coroutine")))
        )
      )
    )
  }

  private def transformSuspensionsSuspendingStateMachineAux(
      tree: tpd.DefDef,
      suspensionPoints: List[tpd.Tree],
      suspensionInReturnedValue: Boolean)(using ctx: Context): tpd.Thicket =
    val suspensionPointsVal: List[tpd.Tree] =
      suspensionPoints.collect { case vd: tpd.ValDef => vd }
    val suspensionPointsSize = suspensionPoints.size

    val continuationClassRef = requiredClassRef(continuationFullName)
    val continuationModule = requiredModule(continuationFullName)
    val safeContinuationClass = requiredClass("continuations.SafeContinuation")
    val continuationImplClass = requiredClass("continuations.jvm.internal.ContinuationImpl")
    val baseContinuationImplClassRef = requiredClassRef(
      "continuations.jvm.internal.BaseContinuationImpl")

    val parent = tree.symbol
    val treeOwner = parent.owner
    val defName = parent.name

    val (rhs, contextFunctionOwner) =
      getBodyContextFunctionOwner(tree)

    val suspendedState =
      ref(continuationModule).select(termName("State")).select(termName("Suspended"))

    val suspendedType: Types.NamedType = suspendedState.symbol.namedType

    val integerOR =
      defn.IntClass.requiredMethod(nme.OR, List(defn.IntType))
    val integerAND =
      defn.IntClass.requiredMethod(nme.AND, List(defn.IntType))
    val integerNE =
      defn.IntClass.requiredMethod(nme.NE, List(defn.IntType))
    val integerMin =
      ref(requiredModuleRef("scala.Int").select(Names.termName("MinValue")).symbol)

    val intType = defn.IntType
    val anyType = defn.AnyType
    val anyOrNullType = Types.OrNull(anyType)
    val anyNullSuspendedType =
      Types.OrType(Types.OrNull(defn.AnyType), suspendedType, false)

    val stateMachineContinuationClassName = s"${treeOwner.name.show}$$$defName$$1"

    val continuationsStateMachineSymbol = newCompleteClassSymbol(
      treeOwner,
      typeName(stateMachineContinuationClassName),
      SyntheticArtifact,
      List(continuationImplClass.typeRef),
      Scopes.newScope
    ).entered.asClass

    val completionParamName = termName("$completion")
    val resultVarName = termName("$result")
    val labelVarParam = termName("$label")

    val continuationsStateMachineConstructor = {
      val symbol = newConstructor(
        continuationsStateMachineSymbol,
        Flags.Synthetic,
        List(completionParamName),
        List(continuationClassRef.appliedTo(anyOrNullType))
      ).entered.asTerm

      tpd.DefDef(symbol)
    }

    val eitherThrowableAnyNullSuspendedType =
      requiredClassRef("scala.util.Either").appliedTo(
        defn.ThrowableType,
        anyNullSuspendedType
      )

    val continuationStateMachineResult = tpd.ValDef(
      newSymbol(
        continuationsStateMachineSymbol,
        resultVarName,
        Flags.Synthetic | Flags.Mutable,
        eitherThrowableAnyNullSuspendedType).entered,
      Underscore(eitherThrowableAnyNullSuspendedType)
    )

    val continuationStateMachineLabel = tpd.ValDef(
      newSymbol(
        continuationsStateMachineSymbol,
        labelVarParam,
        Flags.Synthetic | Flags.Mutable,
        intType).entered,
      Underscore(intType)
    )

    val continuationsStateMachineThis: tpd.This =
      tpd.This(continuationsStateMachineSymbol)

    val continuationsStateMachineLabelSelect =
      continuationsStateMachineThis.select(labelVarParam)

    val transformedMethod = addCompletionParam(tree).asInstanceOf[tpd.DefDef]
    val transformedMethodParams = transformedMethod.paramss
    val transformedMethodSymbol = transformedMethod.symbol

    val newReturnType =
      Types.OrType(Types.OrNull(tree.tpt.tpe), suspendedState.symbol.namedType, false)

    val transformedMethodCompletionParam = ref(
      transformedMethodParams
        .flatten
        .find(_.symbol.info.hasClassSymbol(requiredClass(continuationFullName)))
        .get
        .symbol)

    val transformedMethodParamsWithoutCompletion =
      transformedMethodParams
        .flatten
        .filterNot(_.symbol.info.hasClassSymbol(requiredClass(continuationFullName)))

    val numTransformedMethodOriginalParams =
      transformedMethodParams.map(_.length).sum - 1

    val $completion = continuationsStateMachineConstructor
      .termParamss
      .flatten
      .find(_.name.matchesTargetName(completionParamName))
      .get
      .symbol

    val continuationStateMachineResultSetter =
      newSymbol(
        continuationsStateMachineSymbol,
        continuationStateMachineResult.symbol.asTerm.name.setterName,
        Method | Flags.Accessor,
        info = MethodType(
          continuationStateMachineResult.symbol.asTerm.info.widenExpr :: Nil,
          defn.UnitType)
      ).entered

    val continuationStateMachineLabelSetter =
      newSymbol(
        continuationsStateMachineSymbol,
        continuationStateMachineLabel.symbol.asTerm.name.setterName,
        Method | Flags.Accessor,
        info = MethodType(
          continuationStateMachineLabel.symbol.asTerm.info.widenExpr :: Nil,
          defn.UnitType)
      ).entered

    val (nonDefDefRowsBeforeSuspensionPoint, rowsAfterLastSuspensionPoint)
        : (Map[tpd.Tree, List[tpd.Tree]], List[tpd.Tree]) =
      rhs
        .toList
        .flatMap {
          case Trees.Block(trees, tree) => trees :+ tree
          case tree => List(tree)
        }
        .foldLeft((0, Map.empty[tpd.Tree, List[tpd.Tree]], List.empty[tpd.Tree])) {
          case ((suspensionPointsCounter, nonDefDefRowsBefore, rowsAfterLast), row) =>
            if (treeCallsSuspend(row) || valDefTreeCallsSuspend(row))
              val rowsBefore =
                row match
                  case _: tpd.DefDef =>
                    nonDefDefRowsBefore
                  case _ =>
                    nonDefDefRowsBefore.updatedWith(suspensionPoints(suspensionPointsCounter)) {
                      case None => Option(List.empty)
                      case rows => rows
                    }

              (suspensionPointsCounter + 1, rowsBefore, rowsAfterLast)
            else if (suspensionPointsCounter < suspensionPointsSize)
              val rowsBefore =
                row match
                  case _: tpd.DefDef =>
                    nonDefDefRowsBefore
                  case _ =>
                    nonDefDefRowsBefore.updatedWith(suspensionPoints(suspensionPointsCounter)) {
                      case Some(ll) => Option(ll :+ row)
                      case None => Option(row :: Nil)
                    }

              (suspensionPointsCounter, rowsBefore, rowsAfterLast)
            else (suspensionPointsCounter, nonDefDefRowsBefore, row :: rowsAfterLast)
        }
        .drop(1)

    val nonDefDefRowsBeforeSuspensionPointList = nonDefDefRowsBeforeSuspensionPoint.toList

    def toTreeBeforeSuspend(i: Int): List[tpd.Tree] = {
      val (suspend, rows) = nonDefDefRowsBeforeSuspensionPointList(i)
      val rowsAfter =
        nonDefDefRowsBeforeSuspensionPoint.drop(i + 1).values.toList.flatten ++
          nonDefDefRowsBeforeSuspensionPoint.drop(i + 1).keySet ++
          rowsAfterLastSuspensionPoint

      (rows :+ suspend).collect { case vd: tpd.ValDef => vd }.flatMap { vd =>
        rowsAfter.flatMap(_.shallowFold(List.empty[tpd.Tree]) {
          case (usedTrees, Trees.Inlined(call, _, _)) =>
            call.filterSubTrees(_.symbol.coord == vd.symbol.coord) ++ usedTrees
          case (usedTrees, tree) =>
            if (tree.symbol.coord == vd.symbol.coord) tree :: usedTrees
            else usedTrees
        })
      }
    }

    val treesBeforeSuspendUsedAfterwards: List[tpd.Tree] =
      Range(0, nonDefDefRowsBeforeSuspensionPointList.size)
        .toList
        .flatMap(toTreeBeforeSuspend)
        .distinctBy(_.symbol.coord)

    val distinctVars: List[Symbol] =
      (treesBeforeSuspendUsedAfterwards ++ suspensionPointsVal)
        .distinctBy(_.symbol.coord)
        .map(_.symbol)

    def toGlobalVarSym(symbol: Symbol): TermSymbol =
      newSymbol(
        transformedMethodSymbol,
        symbol.asTerm.name,
        Local | Mutable | Synthetic,
        symbol.info,
        coord = symbol.coord).entered

    val globalVarsSyms = distinctVars.map(toGlobalVarSym)

    def toParamsAsVal(vd: tpd.ValDef): TermSymbol =
      newSymbol(
        transformedMethodSymbol,
        termName(vd.name.toString + "##1"),
        Local | Mutable | Synthetic,
        vd.symbol.info,
        coord = vd.symbol.coord
      ).entered

    val transformedMethodParamsValDefs: List[tpd.ValDef] =
      transformedMethodParamsWithoutCompletion.collect { case vd: tpd.ValDef => vd }

    val transformedMethodParamsAsValSyms: List[TermSymbol] =
      transformedMethodParamsValDefs.map(toParamsAsVal)

    val transformedMethodParamsAsVals: List[tpd.Tree] =
      transformedMethodParamsAsValSyms.zip(transformedMethodParamsValDefs).map {
        case (sym, vd) => tpd.ValDef(sym, ref(vd.symbol))
      }

    def contFsmSym(i: Int): TermSymbol =
      newSymbol(
        continuationsStateMachineSymbol,
        termName(s"I$$$i"),
        Flags.Synthetic | Flags.Mutable,
        anyType).entered

    val continuationStateMachineI$NsSyms: List[TermSymbol] = {
      val numVal = transformedMethodParamsValDefs.size + distinctVars.size
      Range(0, numVal)
        .toList
        .map(
          contFsmSym
        )
    }

    val continuationStateMachineClass: tpd.TypeDef = {
      def contFsmI(sym: TermSymbol): tpd.ValDef =
        tpd.ValDef(sym, Underscore(anyType))

      def toContFsmSetter(symbol: Symbol): tpd.DefDef = {
        val sym = newSymbol(
          continuationsStateMachineSymbol,
          symbol.asTerm.name.setterName,
          Method | Flags.Accessor,
          info = MethodType(symbol.asTerm.info.widenExpr :: Nil, defn.UnitType)
        ).entered
        tpd.DefDef(sym, tpd.unitLiteral)
      }

      val invokeSuspendSymbol =
        newSymbol(
          continuationsStateMachineSymbol,
          Names.termName("invokeSuspend"),
          Flags.Override | Flags.Protected | Flags.Method,
          MethodType(
            List(termName("result")),
            List(eitherThrowableAnyNullSuspendedType),
            anyOrNullType
          )
        ).entered.asTerm

      val invokeSuspendMethod = tpd.DefDef(
        invokeSuspendSymbol,
        paramss =>
          tpd.Block(
            List(
              tpd
                .Assign(continuationsStateMachineThis.select(resultVarName), paramss.head.head),
              tpd.Assign(
                continuationsStateMachineLabelSelect,
                continuationsStateMachineLabelSelect.select(integerOR).appliedTo(integerMin)
              )
            ), {
              val methodSymbol = transformedMethodSymbol
              println(s"methodSymbol.paramSymss: ${methodSymbol.paramSymss}")
              methodSymbol
                .paramSymss
                .foldLeft(Option.empty[tpd.Tree]) { (acc, nextArgs) =>
                  if (nextArgs.exists(_.isType))
                    val nextArgsAsNothing = nextArgs.map(_ => ctx.definitions.NothingType)
                    acc
                      .map(_.appliedToTypes(nextArgsAsNothing))
                      .orElse(Some(ref(methodSymbol).appliedToTypes(nextArgsAsNothing)))
                  else
                    val args = nextArgs.map { sym =>
                      if (sym.info.hasClassSymbol(requiredClass(continuationFullName))) {
                        continuationsStateMachineThis
                      } else nullLiteral
                    }
                    acc
                      .map(_.appliedToArgs(args))
                      .orElse(Some(ref(methodSymbol).appliedToArgs(args)))
                }
                .get
            }
          )
      )
      val createSymbol =
        newSymbol(
          continuationsStateMachineSymbol,
          Names.termName("create"),
          Flags.Override | Flags.Method,
          MethodType(
            List(termName("value"), termName("completion")),
            List(anyOrNullType, continuationClassRef.appliedTo(anyOrNullType)),
            continuationClassRef.appliedTo(defn.UnitType)
          )
        ).entered.asTerm

      val createMethod = tpd.DefDef(
        createSymbol,
        paramss =>
          tpd
            .New(baseContinuationImplClassRef)
            .select(nme.CONSTRUCTOR)
            .appliedTo(paramss.head.drop(1).head)
      )

      val invokeSymbol =
        newSymbol(
          continuationsStateMachineSymbol,
          Names.termName("invoke"),
          Flags.Protected | Flags.Method,
          MethodType(
            List(termName("p1"), termName("p2")),
            List(anyOrNullType, continuationClassRef.appliedTo(anyOrNullType)),
            anyOrNullType
          )
        ).entered.asTerm

      val invokeMethod = tpd.DefDef(
        invokeSymbol,
        paramss =>
          val newContinuation = ref(createMethod.symbol).appliedToTermArgs(
            List(paramss.head.head, paramss.head.drop(1).head)
          )
          val dummy = tpd
            .New(requiredClassRef("scala.util.Right"))
            .select(nme.CONSTRUCTOR)
            .appliedToTypes(
              List(defn.UnitType, defn.UnitType)
            )
            .appliedTo(
              tpd.Literal(Constant(()))
            )

          newContinuation
            .select(nme.asInstanceOf_)
            .appliedToType(baseContinuationImplClassRef.symbol.thisType)
            .select(termName("invokeSuspend"))
            .appliedTo(
              dummy
            )
      )

      val extendsContImpl: tpd.Tree =
        tpd
          .New(ref(continuationImplClass))
          .select(nme.CONSTRUCTOR)
          .appliedTo(
            ref($completion),
            ref($completion).select(termName("context"))
          )

      ClassDefWithParents(
        cls = continuationsStateMachineSymbol,
        constr = continuationsStateMachineConstructor,
        parents = List(extendsContImpl),
        body = List(
          continuationStateMachineI$NsSyms.map(contFsmI),
          continuationStateMachineI$NsSyms.map(toContFsmSetter),
          List(
            continuationStateMachineResult,
            continuationStateMachineLabel,
            tpd.DefDef(continuationStateMachineResultSetter, tpd.unitLiteral),
            tpd.DefDef(continuationStateMachineLabelSetter, tpd.unitLiteral),
            invokeSuspendMethod,
            createMethod,
            invokeMethod
          )
        ).flatten
      )
    }

    def transformSuspendTree(newParent: Symbol) = {
      val contSymbol: TermSymbol =
        newSymbol(
          newParent,
          termName("$continuation"),
          Local | Synthetic,
          continuationStateMachineClass.tpe).entered

      val completionMatch = {
        /* ```
         case x$0: ${contFsmClass} if x$0.$label & Integer.MinValue != 0 =>
           x$0.$label = x$0.label - Integer.MinValue
           x$0
         ```*/
        val case11: tpd.CaseDef = {
          val param =
            newSymbol(
              newParent,
              nme.x_0,
              Flags.Case | Flags.CaseAccessor,
              continuationStateMachineClass.tpe
            ).entered

          val paramLabel = ref(param).select(labelVarParam)

          tpd.CaseDef(
            tpd.Bind(param, tpd.Typed(ref(param), ref(continuationsStateMachineSymbol))),
            ref(param)
              .select(labelVarParam)
              .select(integerAND)
              .appliedTo(integerMin)
              .select(integerNE)
              .appliedTo(tpd.Literal(Constant(0x0))),
            tpd.Block(
              List(tpd.Assign(paramLabel, paramLabel.select(defn.Int_-).appliedTo(integerMin))),
              ref(param)
            )
          )
        }
        /* ```
         case _ => new ${contFsmClass}($completion)
        ```*/
        val case12 = tpd.CaseDef(
          Underscore(anyType),
          tpd.EmptyTree,
          tpd
            .New(tpd.TypeTree(continuationStateMachineClass.tpe))
            .select(nme.CONSTRUCTOR)
            .appliedTo(transformedMethodCompletionParam)
        )
        tpd.Match(transformedMethodCompletionParam, List(case11, case12))
      }
      println(s"completionMatch: ${completionMatch.show}")

      val getResult = ref(contSymbol).select(resultVarName)

      val callToCheckResult =
        ref(requiredModule(continuationFullName))
          .select(termName("checkResult"))
          .appliedTo(getResult)

      val labels: List[Symbol] =
        nonDefDefRowsBeforeSuspensionPoint.keySet.toList.indices.toList.map { i =>
          newSymbol(newParent, termName(s"label$i"), Flags.Label, defn.UnitType).entered
        }

      def rowsBeforeSuspendIncludingSuspend(index: Int) =
        nonDefDefRowsBeforeSuspensionPoint
          .take(index + 1)
          .transform { (suspend, rowsBefore) => rowsBefore :+ suspend }
          .values
          .toList
          .flatten

      def paramsAndRowsBeforeSuspendIncludingSuspend(index: Int) =
        transformedMethodParamsAsVals ++ rowsBeforeSuspendIncludingSuspend(index)

      def treesToAssignToI$Ns(stateIx: Int): List[TermSymbol] =
        val paramsAndRowsUpToSuspend: List[tpd.Tree] =
          paramsAndRowsBeforeSuspendIncludingSuspend(stateIx).dropRight(1)
        (transformedMethodParamsAsValSyms ++ globalVarsSyms)
          .filter(v => paramsAndRowsUpToSuspend.exists(_.symbol.denot.matches(v)))
          .sortWith { (s1, s2) =>
            paramsAndRowsUpToSuspend.indexWhere(_.symbol == s1) >
              paramsAndRowsUpToSuspend.indexWhere(_.symbol == s2)
          }

      def frameVar(i: Int): tpd.Tree =
        ref(contSymbol).select(continuationStateMachineI$NsSyms(i))
      def assignToI(sym: TermSymbol, i: Int): tpd.Assign =
        tpd.Assign(frameVar(i), ref(sym))
      def assignFromI(sym: TermSymbol, i: Int): tpd.Assign =
        tpd.Assign(ref(sym), frameVar(i))

      def toStateCase(stateIx: Int): tpd.CaseDef =
        val (suspension, rowsBefore) = nonDefDefRowsBeforeSuspensionPointList(stateIx)
        val insertLabel =
          if (stateIx > 0) tpd.Labeled(labels(stateIx).asTerm, tpd.EmptyTree) else tpd.EmptyTree

        val callSuspensionPoint: tpd.Tree = suspension match
          case vd: tpd.ValDef if vd.rhs.isInstanceOf[tpd.Inlined] =>
            new TreeTypeMap(
              oldOwners = List(parent),
              newOwners = List(newParent),
              substFrom = List(parent),
              substTo = List(newParent)
            ).transform(vd.rhs.asInstanceOf[tpd.Inlined].call)
          case tpd.Inlined(call, _, _) =>
            new TreeTypeMap(
              oldOwners = List(parent),
              newOwners = List(newParent),
              substFrom = List(parent),
              substTo = List(newParent)
            ).transform(call)
          case _ => tpd.EmptyTree

        val safeContinuation: tpd.ValDef = {
          val shiftType = callSuspensionPoint.tpe

          val safeContinuationConstructor =
            ref(requiredModule("continuations.SafeContinuation"))
              .select(termName("init"))
              .appliedToType(shiftType)
              .appliedTo(ref(contSymbol))

          tpd.ValDef(
            newSymbol(
              newParent,
              termName("safeContinuation"),
              Flags.Local,
              safeContinuationConstructor.tpe).entered,
            safeContinuationConstructor)
        }

        def assignGlobalVarResult(vd: tpd.ValDef): tpd.Assign =
          tpd.Assign(
            ref(globalVarsSyms.find(matchesNameCoord(_, vd)).get),
            getResult.select(nme.asInstanceOf_).appliedToType(vd.symbol.info)
          )

        val assignResultToGlobalVar =
          nonDefDefRowsBeforeSuspensionPoint.keySet.toList.lift(stateIx - 1) match
            case Some(vd: tpd.ValDef) => assignGlobalVarResult(vd)
            case _ => tpd.EmptyTree

        val assignToIPairs: List[(TermSymbol, Int)] =
          treesToAssignToI$Ns(stateIx).zipWithIndex

        val relevantParamsAndRows =
          paramsAndRowsBeforeSuspendIncludingSuspend(stateIx)
            .reverse
            .drop(1)
            .dropWhile(r => !treeCallsSuspend(r) && !valDefTreeCallsSuspend(r))
            .drop(1)

        def needsAssignFrom(sym: TermSymbol): Boolean =
          relevantParamsAndRows.exists(_.symbol.denot.matches(sym))

        def updateForGlobalVars(tree: tpd.Tree): tpd.Tree =
          tree match
            case vd: tpd.ValDef =>
              val gvSym = globalVarsSyms.find(_.denot.matches(vd.symbol))
              gvSym.fold(vd)(gv => tpd.Assign(ref(gv), vd.rhs))
            case _ => tree

        val incrementLabel =
          tpd.Assign(ref(contSymbol).select(labelVarParam), tpd.Literal(Constant(stateIx + 1)))

        val orThrowMatch: tpd.Match = {
          val orThrowSymbol: TermSymbol =
            newSymbol(
              newParent,
              termName("orThrow"),
              Flags.Case | Flags.CaseAccessor,
              anyNullSuspendedType).entered

          val assignGetOrThrowToGlobalVar =
            nonDefDefRowsBeforeSuspensionPoint.keySet.toList(stateIx) match
              case vd: tpd.ValDef =>
                tpd.Assign(
                  ref(globalVarsSyms.find(matchesNameCoord(_, vd)).get),
                  ref(orThrowSymbol).select(nme.asInstanceOf_).appliedToType(vd.symbol.info)
                ) :: Nil
              case _ =>
                Nil

          val returnToLabel =
            if (suspensionPointsSize > 1 && stateIx < suspensionPointsSize - 1)
              List(tpd.Return(unitLiteral, labels(stateIx + 1)))
            else
              Nil

          val resultValue =
            if (stateIx == suspensionPointsSize - 1 && suspensionInReturnedValue)
              List(ref(orThrowSymbol))
            else Nil

          val sts = List(assignGetOrThrowToGlobalVar, returnToLabel, resultValue).flatten
          tpd.Match(
            ref(safeContinuation.symbol).select(termName("getOrThrow")).appliedToNone,
            List(
              tpd.CaseDef(suspendedState, tpd.EmptyTree, tpd.Return(suspendedState, newParent)),
              tpd.CaseDef(tpd.Bind(orThrowSymbol, tpd.EmptyTree), tpd.EmptyTree, blockOf(sts))
            )
          )
        }

        val stats: List[tpd.Tree] = List(
          assignToIPairs.filter(p => needsAssignFrom(p._1)).map(assignFromI),
          List(callToCheckResult),
          List(assignResultToGlobalVar),
          List(insertLabel),
          rowsBefore.map(updateForGlobalVars),
          assignToIPairs.map(assignToI),
          List(
            incrementLabel,
            safeContinuation,
            transformSuspendContinuationBody(callSuspensionPoint, safeContinuation.symbol),
            orThrowMatch
          )
        ).flatten

        tpd.CaseDef(tpd.Literal(Constant(stateIx)), tpd.EmptyTree, blockOf(stats))
      end toStateCase

      val lastCase = {
        val lastStatement = suspensionPoints.lastOption match {
          case Some(vd: tpd.ValDef) =>
            tpd.Assign(
              ref(globalVarsSyms.find(matchesNameCoord(_, vd)).get),
              getResult.select(nme.asInstanceOf_).appliedToType(vd.symbol.info)
            ) :: Nil
          case Some(_) if suspensionInReturnedValue => getResult :: Nil
          case _ => Nil
        }

        val stats =
          treesToAssignToI$Ns(suspensionPoints.size - 1).zipWithIndex.map(assignFromI)

        tpd.CaseDef(
          tpd.Literal(Constant(suspensionPointsSize)),
          tpd.EmptyTree,
          blockOf(stats ++ List(callToCheckResult) ++ lastStatement)
        )
      }

      val block = blockOf(
        List(
          tpd.ValDef(contSymbol, completionMatch),
          tpd.Match(
            ref(contSymbol).select(labelVarParam),
            Range(0, nonDefDefRowsBeforeSuspensionPointList.size).toList.map(toStateCase) ++
              List(lastCase, wrongStateCase))
        ))
        println(s"block: ${block.show}")
        block

    }

    val transformedMethodParamSymbols: List[Symbol] =
      transformedMethodSymbol.paramSymss.flatMap(_.filterNot(hasContinuationClass))

    val oldMethodParamSymbols: List[Symbol] =
      tree.symbol.paramSymss.flatten.filterNot(s => hasSuspendClass(s) || s.isTypeParam)

    /**
     * If there are more than one Suspension points we create the whole state machine for all
     * the points and replace the last occurrence of `shift`. The other occurrences are not
     * needed and are being removed.
     *
     * We also don't need to keep the rest of the lines before the last `shift` as they are
     * embedded in the state machine.
     */
    var c = 0
    var rowsToRemove =
      nonDefDefRowsBeforeSuspensionPoint.values.toList.flatten.map(_.symbol.coord)
    val substituteContinuation = new TreeTypeMap(
      treeMap = {
        case tree if valDefTreeCallsSuspend(tree) && c < suspensionPointsSize - 1 =>
          c += 1
          tpd.EmptyTree
        case tree if valDefTreeCallsSuspend(tree) && c == suspensionPointsSize - 1 =>
          val suspendTree = transformSuspendTree(transformedMethodSymbol)
          println(s"tree: ${tree.show}")
          println(s"suspendTree: ${suspendTree.show}")
          suspendTree
        case tree if treeCallsSuspend(tree) && c < suspensionPointsSize - 1 =>
          c += 1
          tpd.EmptyTree
        case tree if treeCallsSuspend(tree) && c == suspensionPointsSize - 1 =>
          val suspendTree = transformSuspendTree(transformedMethodSymbol)
          println(s"tree: ${tree.show}")
          println(s"suspendTree: ${suspendTree.show}")
          suspendTree
        case tree if c < suspensionPointsSize && rowsToRemove.contains(tree.symbol.coord) =>
          rowsToRemove = rowsToRemove.zipWithIndex.collect {
            case (coord, index) if index != rowsToRemove.indexOf(tree.symbol.coord) =>
              coord
          }
          tpd.EmptyTree
        case tree if globalVarsSyms.exists(gv => matchesNameCoord(gv, tree)) =>
          println(s"tree matchesNameCoord: ${tree.show}")
          ref(globalVarsSyms.find(gv => matchesNameCoord(gv, tree)).get)
        case tt
            if tt.symbol.exists &&
              tt.symbol.is(TermParam) &&
              tt.symbol.owner.denot.matches(tree.symbol) &&
              transformedMethodParamsWithoutCompletion.exists(
                _.symbol.denot.matches(tt.symbol)) &&
            transformedMethodParamsAsValSyms.exists(fieldMatchesParam(_, tt.symbol)) =>
          println(s"tt: ${tt.show}")
          val newtt = ref(transformedMethodParamsAsValSyms.find(p => fieldMatchesParam(p, tt.symbol)).get)
          println(s"newtt: ${newtt.show}")
          newtt
        case tree =>
          println(s"substituteContinuation unmatched tree: ${tree.show}")
          tree
      },
      substFrom = List(parent) ++ oldMethodParamSymbols ++ contextFunctionOwner.toList,
      substTo = List(transformedMethodSymbol) ++ transformedMethodParamSymbols ++
        List(transformedMethodSymbol),
      oldOwners = List(parent) ++ contextFunctionOwner.toList,
      newOwners = List(transformedMethodSymbol, transformedMethodSymbol)
    )

    def toGlobalVar(sym: Symbol): tpd.ValDef =
      tpd.ValDef(toGlobalVarSym(sym), nullLiteral)

    val bodySubstitute = substituteContinuation.transform(rhs)
    println(s"bodySubstitute: ${bodySubstitute.show}.")

    val transformedMethodBody =
      bodySubstitute match
        case Trees.Block(stats, expr) =>
          flattenBlock(
            blockOf(
              transformedMethodParamsAsVals ++
                globalVarsSyms.map(toGlobalVar) ++
                stats ++
                List(expr)
            ))
        case tree => tree
    println(s"transformedMethodBody: ${transformedMethodBody.show}")
    println(s"rhs: ${rhs.show}")
    tpd.Thicket(
      List(
        continuationStateMachineClass,
        cpy.DefDef(transformedMethod)(rhs = transformedMethodBody)
      ))
  end transformSuspensionsSuspendingStateMachineAux
