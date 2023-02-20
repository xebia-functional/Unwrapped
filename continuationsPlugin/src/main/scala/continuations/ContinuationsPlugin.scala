package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
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
 * fooTest(completion: continuations.Continuation[Int])`.
 */
class ContinuationsCallsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsCallsPhase.name

  override val runsAfter = Set(ContinuationsPhase.name)
  override val runsBefore = Set(PickleQuotes.name)

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[Tree] = ListBuffer.empty

  private def existsTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.toList.find { s =>
      tree.existsSubTree(t => s.name == t.symbol.name && s.coord == t.symbol.coord)
    }

  private def findTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.toList.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
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
          if existsTree(tree).nonEmpty &&
            tree.filterSubTrees(CallsSuspendParameter.unapply(_).nonEmpty).nonEmpty &&
            !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty) =>
        applyToChange.addOne(tree)
      case Apply(Select(_, selected), _)
          if existsTree(tree).nonEmpty &&
            selected.asTermName == nme.apply &&
            tree.filterSubTrees(CallsSuspendParameter.unapply(_).nonEmpty).nonEmpty &&
            !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty) =>
        applyToChange.addOne(tree)
      case Apply(TypeApply(Select(_, selected), _), _)
          if existsTree(tree).nonEmpty &&
            selected.asTermName == nme.apply &&
            tree.filterSubTrees(CallsSuspendParameter.unapply(_).nonEmpty).nonEmpty &&
            !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty) =>
        applyToChange.addOne(tree)
      case Apply(Ident(_), _)
          if findTree(tree).nonEmpty &&
            tree.filterSubTrees(CallsSuspendParameter.unapply(_).nonEmpty).nonEmpty &&
            !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty) =>
        applyToChange.addOne(tree)
      case _ =>
        ()
    ctx

  override def transformApply(tree: Apply)(using ctx: Context): Tree =
    val continuation: Tree = ref(
      requiredModule("continuations.jvm.internal.ContinuationStub").requiredMethod("contImpl"))

    if (applyToChange.exists(_.sameTree(tree)))
      val (paramsNonCF, paramsCF) =
        tree.deepFold((List(List.empty[Tree]), List(List.empty[Tree]))) {
          case (
                (accNonCF, accCF),
                Apply(TypeApply(Select(qualifier, selected), argsType), args))
              if selected.asTermName == nme.apply &&
                existsTree(qualifier).exists(_.is(Flags.Method)) =>
            (
              accNonCF,
              accCF
                .prepended(args.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName))))
                .prepended(
                  argsType.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))))
          case ((accNonCF, accCF), Apply(Select(qualifier, selected), args))
              if selected.asTermName == nme.apply &&
                existsTree(qualifier).exists(_.is(Flags.Method)) =>
            (
              accNonCF,
              accCF.prepended(
                args.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))))
          case ((accNonCF, accCF), Apply(fun, args))
              if existsTree(fun).exists(_.is(Flags.Method)) =>
            (
              accNonCF.prepended(
                args.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))),
              accCF)
          case (acc, _) => acc
        }

      paramsCF
        .filterNot(_.isEmpty)
        .foldLeft(ref(existsTree(tree).get)
          .appliedToTermArgs(paramsNonCF.flatten :+ continuation): Tree) { (parent, value) =>
          if (value.exists {
              case _: TypeTree => true
              case _ => false
            })
            parent.select(nme.apply).appliedToTypeTrees(value)
          else
            parent match
              case _: TypeApply => parent.appliedToTermArgs(value)
              case _ => parent.select(nme.apply).appliedToTermArgs(value)
        }
    else tree

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"
}
