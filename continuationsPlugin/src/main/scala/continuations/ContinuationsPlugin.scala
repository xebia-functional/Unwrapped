package continuations

import dotty.tools.dotc.ast.Trees.ParamClause
import dotty.tools.dotc.ast.{tpd, TreeTypeMap, Trees}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.ast.untpd.Modifiers
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Types}
import dotty.tools.dotc.core.Names.{termName, typeName, Name}
import dotty.tools.dotc.core.StdNames.{nme, tpnme}
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType, PolyType}
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ContinuationsPlugin extends StandardPlugin:

  val name: String = "continuations"
  override val description: String = "CPS transformations"

  def init(options: List[String]): List[PluginPhase] =
    (new ContinuationsPhase) :: new ContinuationsCallsPhase :: Nil

class ContinuationsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsPhase.name

  override def changesBaseTypes: Boolean = true

  override def changesMembers: Boolean = true

  override val runsAfter = Set(Staging.name)
  override val runsBefore = Set(ContinuationsCallsPhase.name)

  override def transformValDef(tree: ValDef)(using Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

end ContinuationsPhase

object ContinuationsPhase {
  val name = "continuations"
}

/**
 * Transform calls `foo()` or `foo()(continuations.Suspend.given_Suspend)` to
 * `foo(ContinuationStub.contImpl)` for `def foo()(using s: Suspend)`.
 *
 * In phase `ContinuationsPhase` the `def foo()(using s: Suspend)` has been transformed to `def
 * foo(completion: continuations.Continuation[Int])`.
 */
class ContinuationsCallsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsCallsPhase.name

  override val runsAfter = Set(ContinuationsPhase.name)
  override val runsBefore = Set(PickleQuotes.name)

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[Tree] = ListBuffer.empty

  private def existsTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find { s =>
      tree.existsSubTree(t => s.name == t.symbol.name && s.coord == t.symbol.coord)
    }

  private def findTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  private def treeIsSuspendAndNotInApplyToChange(tree: Apply)(using Context): Boolean =
    tree.filterSubTrees(CallsSuspendParameter.apply).nonEmpty
  // && !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty)

  private def treeExistsIsApplyAndIsNotInApplyToChange(tree: Apply, n: Name)(
      using Context): Boolean =
    existsTree(tree).nonEmpty &&
      n.asTermName == nme.apply &&
      treeIsSuspendAndNotInApplyToChange(tree)

  private def removeSuspend(trees: List[Tree])(using Context): List[Tree] =
    trees.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))

  private def treeExistsAndIsMethod(tree: Tree)(using Context): Boolean =
    existsTree(tree).exists(_.is(Flags.Method))

  private def treeExistsIsApplyAndIsMethod(tree: Tree, n: Name)(using Context): Boolean =
    n.asTermName == nme.apply &&
      treeExistsAndIsMethod(tree)

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    val starterClassRef = requiredClassRef(starterClassName)
    tree match
      case tree @ DefDef(_, paramss, _, _)
          if !paramss.exists(
            _.exists(_.symbol.info.hasClassSymbol(starterClassRef.classSymbol))) && tree
            .symbol
            .isAnonymousFunction =>
        updatedMethods.addOne(tree.symbol)
      case _ => ()

    val hasContinuationParam =
      tree.termParamss.flatten.exists { p =>
        !p.symbol.is(Flags.Given) &&
        p.tpt.tpe.matches(requiredClassRef(continuationFullName)) &&
        p.name.toString == completionParamName
      }

    if (hasContinuationParam) updatedMethods.addOne(tree.symbol)

    ctx

  override def prepareForApply(tree: Apply)(using Context): Context =
    tree match
      case Apply(Apply(_, _), _)
          if existsTree(tree).nonEmpty && treeIsSuspendAndNotInApplyToChange(tree) =>
        applyToChange.addOne(tree)
      case Apply(Select(_, selected), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(tree, selected) =>
        applyToChange.addOne(tree)
      case Apply(TypeApply(Select(_, selected), _), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(tree, selected) =>
        applyToChange.addOne(tree)
      case Apply(Ident(_), _)
          if findTree(tree).nonEmpty && treeIsSuspendAndNotInApplyToChange(tree) =>
        applyToChange.addOne(tree)
      case _ =>
        ()
    ctx

  override def transformApply(tree: Apply)(using ctx: Context): Tree =
    if (tree.symbol.showFullName == "continuations.jvm.internal.SuspendApp.apply")

      val continuationClassRef = requiredClassRef(continuationFullName)
      val starterClassRef = requiredClassRef(starterClassName)

      val maybeOwner =
        tree
          .filterSubTrees(st => applyToChange.exists(_.sameTree(st)))
          .map(_.symbol.maybeOwner)
          .lastOption

      val owner =
        if maybeOwner.exists(_.isDefinedInSource) then maybeOwner.get
        else {
          val possible = tree.filterSubTrees {
            case tree @ DefDef(_, paramss, _, _)
                if tree.existsSubTree(st => applyToChange.exists(_.sameTree(st))) && !paramss
                  .exists(
                    _.exists(_.symbol.info.hasClassSymbol(starterClassRef.classSymbol))) =>
              true
            case _ => false
          }

          possible.headOption.fold(updatedMethods.toList.last)(_.symbol)
        }

      val starterClassSymbol = newCompleteClassSymbol(
        owner,
        tpnme.ANON_CLASS,
        Flags.SyntheticArtifact | Flags.Private | Flags.Final,
        List(starterClassRef.classSymbol.typeRef),
        Scopes.newScope
      ).entered.asClass

      val constructor = {
        val symbol = newConstructor(
          starterClassSymbol,
          Flags.Synthetic,
          List.empty,
          List.empty
        ).entered.asTerm
        DefDef(symbol)
      }

      val invokeSymbol =
        newSymbol(
          starterClassSymbol,
          Names.termName("invoke"),
          Flags.Override | Flags.Method,
          Types.PolyType(List(Names.typeName("A")))(
            _ => List(Types.TypeBounds(ctx.definitions.NothingType, ctx.definitions.AnyType)),
            pt => {
              MethodType(
                List(termName("completion")),
                List(continuationClassRef.appliedTo(pt.newParamRef(0))),
                Types.OrNull(OrType(pt.newParamRef(0), defn.AnyType, true))
              )
            }
          )
        ).entered.asTerm

      val invokeMethod: DefDef = tpd.DefDef(
        invokeSymbol,
        paramss =>

          val (paramsNonCF, _) =
            tree.deepFold((List(List.empty[Tree]), List(List.empty[Tree]))) {
              case (
                    (accNonCF, accCF),
                    Apply(TypeApply(Select(qualifier, selected), argsType), args))
                  if treeExistsIsApplyAndIsMethod(qualifier, selected) =>
                (
                  accNonCF,
                  accCF.prepended(removeSuspend(args)).prepended(removeSuspend(argsType)))
              case ((accNonCF, accCF), Apply(Select(qualifier, selected), args))
                  if treeExistsIsApplyAndIsMethod(qualifier, selected) =>
                (accNonCF, accCF.prepended(removeSuspend(args)))
              case ((accNonCF, accCF), Apply(fun, args)) if treeExistsAndIsMethod(fun) =>
                (accNonCF.prepended(removeSuspend(args)), accCF)
              case (acc, _) => acc
            }

          ref(existsTree(tree).get).appliedToTermArgs(paramsNonCF.flatten :+ paramss.last.head)
      )

      val newStarterClass = ClassDefWithParents(
        starterClassSymbol,
        constructor,
        List(tpd.New(starterClassRef)),
        List(invokeMethod)
      )

      val newStarterClassConstructor =
        tpd.New(tpd.TypeTree(newStarterClass.tpe)).select(nme.CONSTRUCTOR)

      val substituteContinuationCall = new TreeTypeMap(
        treeMap = {

          case tree @ Block(List(anonFun @ DefDef(_, paramss, _, _)), _)
              if tree.existsSubTree(st => applyToChange.exists(_.sameTree(st))) && paramss
                .exists(_.exists(_.symbol.info.hasClassSymbol(starterClassSymbol))) =>
            anonFun.rhs

          case tree if applyToChange.exists(_.sameTree(tree)) =>
            Block(
              List(
                newStarterClass
              ),
              newStarterClassConstructor
            )
          case tree => tree
        }
      )

      cpy.Apply(tree)(
        fun = tree.fun,
        args = substituteContinuationCall.transform(tree.args)
      )
    else tree

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"
}
