package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Symbols.{requiredClassRef, requiredMethod, requiredModule, Symbol}
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

  override def prepareForApply(tree: Apply)(using Context): Context =
    tree match
      case Apply(_, args) if args.nonEmpty =>
        args.lastOption.foreach {
          case TypeApply(_, List(tt))
              if tt.tpe.typeSymbol == requiredClassRef(continuationFullName).typeSymbol =>
            updatedMethods.addOne(tree.symbol)
          case _ => ()
        }
      case _ => ()
    ctx

  override def transformApply(tree: Apply)(using ctx: Context): Tree =
    val updatedMethodsList: List[Symbol] = updatedMethods.toList

    lazy val findTree =
      updatedMethodsList.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

    tree match
      case Apply(Apply(_, _), List(_))
          if findTree.nonEmpty && CallsSuspendParameter.unapply(tree).nonEmpty =>
        findTree match
          case Some(sym) =>
            ref(sym).appliedTo(
              ref(
                requiredModule("continuations.jvm.internal.ContinuationStub").requiredMethod(
                  "contImpl")))
          case _ => tree
      case _ => tree

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"
}
