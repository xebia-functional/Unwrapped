package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Symbols.{requiredClassRef, requiredMethod, requiredModule, Symbol}
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging
import dotty.tools.dotc.util.Property.StickyKey
import dotty.tools.dotc.util.Store.Location

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

  // prepare for compilation unit
  // what is a situation that we will have the same method name in the same compilation unit that will cause a duplicate?
  override def prepareForUnit(tree: Tree)(using ctx: Context): Context = {
    val newContext = ctx.fresh

    val key = ContinuationsPhase.continuationsPhaseCounterPropertyKey
    val oldKey = ContinuationsPhase.continuationsPhaseOldCounterPropertyKey

    val contextPropertyCounter = newContext.property(key)
    val oldContextPropertyCounter = newContext.property(oldKey) // changed

    if (oldContextPropertyCounter.isEmpty) { //
      val oldCounterLocation = newContext.addLocation(1)
      newContext.setProperty(oldKey, oldCounterLocation)
    }

    if (contextPropertyCounter.isDefined) {
      val oldCounterLocation = newContext.property(oldKey).get
      val currentCounterLocation = newContext.property(key).get

      newContext.updateStore(oldCounterLocation, newContext.store(currentCounterLocation))
      newContext
    } else {
      val oldCounterLocation = newContext.property(oldKey).get
      val counterLocation = newContext.addLocation(1)

      // this is like putting to the store in the oldLocation the value it already had?
      newContext.updateStore(oldCounterLocation, newContext.store(oldCounterLocation))
      newContext.setProperty(key, counterLocation)
    }
  }

  override def prepareForDefDef(tree: DefDef)(using ctx: Context): Context =
    val newContext = ctx.fresh

    val counterLocation =
      newContext.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get
    val oldCount = newContext.store(counterLocation)

    newContext.updateStore(
      counterLocation,
      DefDefTransforms.countContinuationSynthetics(tree, oldCount))

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

end ContinuationsPhase

object ContinuationsPhase {

  val name = "continuations"

  val continuationsPhaseCounterPropertyKey =
    new StickyKey[Location[Int]] {}
  val continuationsPhaseOldCounterPropertyKey =
    new StickyKey[Location[Int]] {}
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

    tree match
      case Apply(Apply(_, _), List(arg))
          if updatedMethodsList.exists(_.name == tree.symbol.name) &&
            arg.symbol.name.toString == "given_Suspend" =>
        val sym = updatedMethodsList.find(_.name == tree.symbol.name).get

        ref(sym).appliedTo(
          ref(
            requiredModule("continuations.jvm.internal.ContinuationStub").requiredMethod(
              "contImpl")))
      case t => t

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"
}
