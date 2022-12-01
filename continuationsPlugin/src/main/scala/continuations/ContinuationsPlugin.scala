package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.util.Store.Location
import dotty.tools.dotc.util.Store
import dotty.tools.dotc.util.Property.Key
import dotty.tools.dotc.util.Property.StickyKey

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

  //format: off
  /**
    * Adds the synthetic counter to the context when the phase is entered on a compilation unit. The counter starts at 1. It can be fetched with:
    *
    ```scala
      summon[Context].property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get
    ```
    * 
    * The value in the location should only be updated in a
    * `prepareFor<N>`. This means that we need to know how many
    * synthetics we will need to generate in the N transform. This
    * shuould be possible via scanning source, then in the transform,
    * working backword from currently set property value. To update in
    * a `prepareFor<N>`:
    * 
    ```scala
    val newContext = ctx.fresh
    val counterLocation = newContext.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get
    val oldCount = newContext.store(counterLocation)
    // scan for necessary synthetics by walking the tree and adding to the old count,
    // returning the new context
    newContext.updateStore(counterLocation, countNecessarySynthetics(oldCount, tree))
    ```
    *
    * @param tree
    * @param ctx
    * @return
    */
  //format: on
  override def prepareForUnit(tree: tpd.Tree)(using ctx: Context): Context = {
    val newContext = ctx.fresh
    val key = ContinuationsPhase.continuationsPhaseCounterPropertyKey
    val oldKey = ContinuationsPhase.continuationsPhaseOldCounterPropertyKey
    val contextPropertyCounter = newContext.property(key)
    val oldContextPropertyCounter = newContext.property(key)
    if(!oldContextPropertyCounter.isDefined){
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
      newContext.updateStore(oldCounterLocation, newContext.store(oldCounterLocation))
      newContext.setProperty(key, counterLocation)
    }
  }

  override def prepareForDefDef(tree: tpd.DefDef)(using ctx:Context): Context =
    val newContext = ctx.fresh
    val counterLocation = newContext.property(ContinuationsPhase.continuationsPhaseCounterPropertyKey).get
    val oldCount = newContext.store(counterLocation)
    newContext.updateStore(counterLocation, DefDefTransforms.countContinuationSynthetics(tree, oldCount)(using newContext))

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

end ContinuationsPhase

object ContinuationsPhase {
  val continuationsPhaseCounterPropertyKey =
    new StickyKey[Location[Int]]{}
  val continuationsPhaseOldCounterPropertyKey =
    new StickyKey[Location[Int]]{}
}
