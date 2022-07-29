package continuations

import dotty.tools.dotc.report
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.StdNames.*
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{AppliedType, Type}
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import dotty.tools.dotc.semanticdb.TypeMessage.SealedValue.TypeRef
import dotty.tools.dotc.transform.{PickleQuotes, Staging}

class ContinuationsPlugin extends StandardPlugin:
  val name: String = "continuations"
  override val description: String = "CPS transformations"

  def init(options: List[String]): List[PluginPhase] =
    (new ContinuationsPhase) :: Nil

class ContinuationsPhase extends PluginPhase:
  import tpd.*

  val phaseName = "continuations"

  override val runsAfter = Set(Staging.name)
  override val runsBefore = Set(PickleQuotes.name)

  override def transformBlock(tree: Block)(using ctx: Context): Tree =
    val calls: List[Apply] = suspensionPoints(tree)
    calls.foreach { call => report.error(s"Found suspension point: ${call.show}") }
    tree

  def suspensionPoints(tree: Block)(using ctx: Context): List[Apply] =
    if (returnsContextFunctionWithSuspendType(tree)) List.empty[Apply]
    else tree.filterSubTrees { case t: Tree => isCallToSuspend(t) }.asInstanceOf

  def isSuspendType(tpe: Type)(using ctx: Context): Boolean =
    tpe.classSymbol.showFullName == "continuations.Suspend"

  def returnsContextFunctionWithSuspendType(tree: Tree)(using ctx: Context): Boolean =
    ctx.definitions.isContextFunctionType(tree.tpe) && tree.tpe.argTypes.exists(isSuspendType)

  def isCallToSuspend(tree: Tree)(using ctx: Context): Boolean =
    tree match
      case Apply(_, _) => returnsContextFunctionWithSuspendType(tree)
      case _ => false
