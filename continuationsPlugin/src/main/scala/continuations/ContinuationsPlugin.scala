package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

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

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    import dotty.tools.dotc.core.Symbols.*
    import dotty.tools.dotc.core.Names
    import dotty.tools.dotc.ast.tpd.*
//    val x = requiredMethod("continuations.Suspend#suspendContinuation")
    val y = requiredClass("continuations.Suspend")
//    println("DONE0")
//    println(
//      ref(y)
//        .select(Names.termName("suspendContinuation"))
//        .appliedTo(ref(y))
//        .appliedToType(ctx.definitions.IntType)
//        .show)
//    println("DONE1")
//    val t: Tree = ref(y).select(Names.termName("suspendContinuation"))
//    println(ref(x).appliedToNone)
    DefDefTransforms.transformSuspendContinuation(tree)

end ContinuationsPhase
