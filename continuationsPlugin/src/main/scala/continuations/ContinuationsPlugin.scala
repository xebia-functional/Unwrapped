package continuations

import dotty.tools.dotc.report
import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.StdNames.*
import dotty.tools.dotc.core.Symbols.*
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

  override def transformDefDef(tree: tpd.DefDef)(implicit ctx: Context): Tree =
    tree.symbol
    tree match
      case DefDef(name, params, AppliedTypeTree(tyCon, args), preRhs) =>
        report.error(s"${tyCon.tpe}, $args")
        tree
      case _ => tree
