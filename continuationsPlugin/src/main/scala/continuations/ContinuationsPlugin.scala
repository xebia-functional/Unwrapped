package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

import scala.collection.mutable

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

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    val hasContinuationParam =
      tree.termParamss.flatten.exists { p =>
        !p.symbol.is(Flags.Given) &&
        p.tpt.tpe.matches(requiredClassRef(continuationFullName)) &&
        p.name.toString == completionParamName
      }

    if (hasContinuationParam) updatedMethods.addOne(tree.symbol)

    ctx

  override def transformApply(tree: Apply)(using ctx: Context): Tree =
    def findTree(tree: Tree): Option[Symbol] =
      updatedMethods
        .toList
        .find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

    val continuation: Tree = ref(
      requiredModule("continuations.jvm.internal.ContinuationStub").requiredMethod("contImpl"))

    val applyArgsWithContinuation: List[Tree] =
      tree
        .filterSubTrees {
          case Apply(_, _) => true
          case _ => false
        }
        .map {
          case Apply(_, args) =>
            args.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))
        }
        .reverse
        .flatten :+ continuation

    tree match
      case Apply(Apply(_, _), _)
          if findTree(tree).nonEmpty && CallsSuspendParameter.unapply(tree).nonEmpty =>
        ref(findTree(tree).get).appliedToTermArgs(applyArgsWithContinuation)
      // method apply when it is a context function
      case Apply(Select(Apply(fn, _), selected), _)
          if findTree(fn).nonEmpty &&
            selected.asTermName.toString == "apply" &&
            CallsSuspendParameter.unapply(tree).nonEmpty =>
        ref(findTree(fn).get).appliedToTermArgs(applyArgsWithContinuation)
      case _ => tree

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"
}
