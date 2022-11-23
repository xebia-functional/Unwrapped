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
    DefDefTransforms.transformSuspendContinuation(tree)

/*
  def isSuspendType(tpe: Type)(using ctx: Context): Boolean =
    tpe.classSymbol.info.hasClassSymbol(Symbols.requiredClass("continuations.Suspend"))

  def returnsContextFunctionWithSuspendType(tree: DefDef)(using ctx: Context): Boolean =
    hasSuspendParam(tree).fold(false) { suspendParamName =>
      tree.rhs.existsSubTree(st => hasInnerSuspensionPoint(st, suspendParamName))
    }

  def hasSuspendParam(tree: tpd.DefDef)(using ctx: Context): Option[ValDef] =
    tree.paramss.reverse match {
      case ValDefs(vparams @ (vparam :: _)) :: _
          if vparam.mods.isOneOf(Flags.GivenOrImplicit) =>
        vparams.find(vp => isSuspendType(vp.tpe))
      case _ => None
    }

  def hasInnerSuspensionPoint(subTree: Tree, suspendParamName: ValDef)(using ctx: Context): Boolean =
    subTree match
      case Inlined(Apply(fun, _), _, _) => isCallToSuspend(fun)
      case Apply(_, args) => isCallToSuspend(args, suspendParamName)
      case _ => false

  def isCallToSuspend(tree: Tree)(using ctx: Context): Boolean =
    val requiredMethod = Symbols.requiredMethod("continuations.Continuation.suspendContinuation")
    tree.symbol.denot.matches(requiredMethod.denot)

  def isCallToSuspend(trees: List[Tree], suspendParamName: ValDef)(using ctx: Context): Boolean =
    trees.exists(_.symbol == suspendParamName.symbol)
 */
end ContinuationsPhase
