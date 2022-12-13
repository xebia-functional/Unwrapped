package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging
import dotty.tools.dotc.util.Property.StickyKey
import dotty.tools.dotc.util.Store.Location

class ContinuationsPlugin extends StandardPlugin:

  val name: String = "continuations"
  override val description: String = "CPS transformations"

  def init(options: List[String]): List[PluginPhase] =
    (new ContinuationsPhase) :: Nil

class ContinuationsPhase extends PluginPhase:

  override def phaseName: String = "continuations"

  override def changesBaseTypes: Boolean = true

  override def changesMembers: Boolean = true

  override val runsAfter = Set(Staging.name)
  override val runsBefore = Set(PickleQuotes.name)

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

  val continuationsPhaseCounterPropertyKey =
    new StickyKey[Location[Int]] {}
  val continuationsPhaseOldCounterPropertyKey =
    new StickyKey[Location[Int]] {}
}
