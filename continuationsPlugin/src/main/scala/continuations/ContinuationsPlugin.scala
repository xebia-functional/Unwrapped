package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Names.Name
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.FreshContext
import dotty.tools.dotc.ast.TreeTypeMap
import dotty.tools.dotc.util.Property.Key
import continuations.ContinuationsCallsPhase.CallerKey
import continuations.ContinuationsCallsPhase.Caller
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.report
import java.io.StringWriter
import java.io.PrintWriter

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
    println("transformValDef")
    DefDefTransforms.transformSuspendContinuation(tree)

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    println("transformDefDef continuationsPhase")
    try{
      DefDefTransforms.transformSuspendContinuation(tree)
    } catch {
      case e: Throwable =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        println("error: ${sw.toString}")
        throw e
    }

end ContinuationsPhase

object ContinuationsPhase {
  val name = "continuations"
}

/**
 * Transform calls `foo()` or `foo()(continuations.Suspend.given_Suspend)` to
 * `foo(ContinuationStub.contImpl)` for `def foo()(using s: Suspend)`.
 *
 * In phase `ContinuationsPhase` the `def foo()(using s: Suspend)` has been transformed to `def
 * foo(completion: continuations.Continuation[Int])`.
 */
class ContinuationsCallsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsCallsPhase.name

  override val runsAfter = Set(ContinuationsPhase.name)
  override val runsBefore = Set(PickleQuotes.name)

  def isSuspend(t: Tree)(using Context): Boolean =
    t.symbol.info.hasClassSymbol(requiredClass(suspendFullName))
  def isGivenSuspend(t: Tree)(using Context): Boolean =
    t.symbol.originalOwner.showFullName == suspendFullName

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[Tree] = ListBuffer.empty

  private def existsTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find { s =>
      tree.existsSubTree(t => s.name == t.symbol.name && s.coord == t.symbol.coord)
    }

  private def findTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  private def treeIsSuspendAndNotInApplyToChange(tree: Apply)(using Context): Boolean =
    tree.filterSubTrees(CallsSuspendParameter.apply).nonEmpty &&
      !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty)

  private def treeExistsIsApplyAndIsNotInApplyToChange(tree: Apply, n: Name)(
      using Context): Boolean =
    existsTree(tree).nonEmpty &&
      n.asTermName == nme.apply &&
      treeIsSuspendAndNotInApplyToChange(tree)

  private def removeSuspend(trees: List[Tree])(using Context): List[Tree] =
    trees.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))

  private def treeExistsAndIsMethod(tree: Tree)(using Context): Boolean =
    existsTree(tree).exists(_.is(Flags.Method))

  private def treeExistsIsApplyAndIsMethod(tree: Tree, n: Name)(using Context): Boolean =
    n.asTermName == nme.apply &&
      treeExistsAndIsMethod(tree)

  def hasContinuationParam(tree: DefDef)(using Context): Boolean =
    tree.termParamss.flatten.exists { p =>
      !p.symbol.is(Flags.Given) &&
      p.tpt.tpe.matches(requiredClassRef(continuationFullName)) &&
      p.name.toString == completionParamName
    }

  /**
   * In this override, we attach the closest caller to all applies in order to ensure that
   * nested suspended definition calls (not nested shifts) receive the outer suspended
   * definition's completion param, which is necessary for proper coroutine scope control. We
   * also detect all the applies that have to change receive a completion argument and remove
   * the Suspend marker trait.
   *
   * @param tree
   *   The def tree to scan.
   * @return
   *   The original context unchanged.
   */
  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    println("prepareForDefDef")
    if (hasContinuationParam(tree))
      updatedMethods.addOne(tree.symbol)
    tree.foreachSubTree {
      case a @ Apply(_, _) if hasContinuationParam(tree) =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(_, _) if hasContinuationParam(tree) =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(Apply(_, _), _)
          if existsTree(tree).nonEmpty && treeIsSuspendAndNotInApplyToChange(a) =>
        applyToChange.addOne(a)
      case a @ Apply(Select(_, selected), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(a, selected) =>
        applyToChange.addOne(a)
      case a @ Apply(TypeApply(Select(_, selected), _), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(a, selected) =>
        applyToChange.addOne(a)
      case a @ Apply(Ident(_), _)
          if findTree(a).nonEmpty && treeIsSuspendAndNotInApplyToChange(a) =>
        applyToChange.addOne(a)
      case t => ()
    }
    ctx

  extension (s: Symbol)
    def firstParamSyms(using Context): Option[( (Symbol, Int),Int)] = s
      .paramSymss.zipWithIndex.find(_._1.exists(_.info.hasClassSymbol(requiredClass(continuationFullName)))).flatMap( pc =>
        
        // reviewer request for comment -- do we have a isContinuation anywhere?
        pc._1.zipWithIndex.find(_._1.info.hasClassSymbol(requiredClass(continuationFullName))).map( foundCompletionPc => (foundCompletionPc, pc._2)))

  /**
   * Because TreeTypeMap recurses, to prevent extra additional completion arguments we manually
   * reconstruct the apply calls to applies that must receive a completion by collecting all the
   * nested Apply tree args. This ensures that the correct completion from the caller or the
   * default completion is correctly substituted for marker Suspend arguments, in the correct
   * argument position, with the correct caller ownership.
   *
   * @param tree
   *   The Apply tree to reconstruct
   * @param accumulator
   *   The nested apply trees' arguments.
   * @return
   *   A new GenericApply with the correct arguments applied with the correct owners
   */
  def deconstructNestedApply(tree: Apply, accumulator: List[List[Tree]])(using Context): Tree =
    println("deconstructNestedApply")
    val defaultContinuation: Tree = ref(
      requiredModule("continuations.jvm.internal.ContinuationStub").requiredMethod("contImpl"))
    tree match {
      case Apply(Select(fn @ Apply(_, _), selected), args) =>
        // println(s"deconstructNestedApply ${tree.show}")
        val newArgs = args.filterNot(isSuspend)
        val deconstructedInnerApply = deconstructNestedApply(fn, Nil)
        if (newArgs.isEmpty) deconstructedInnerApply
        else Select(deconstructNestedApply(fn, Nil), selected).appliedToTermArgs(newArgs)
      case Apply(b @ Apply(_, _), _) =>
        deconstructNestedApply(b, tree.args :: accumulator)
      case Apply(fn, args) =>
        val newSym = existsTree(fn).get
        val symRef = ref(newSym)
        val caller = tree.getAttachment(CallerKey)
        // we can use Option#get in these calls because in
        // prepareForDefDef we have identified all the applies that
        // must change, so we know that the Options will not be None.
        val (_, completionIndex) = newSym.firstParamSyms.get
        println(completionIndex)

        val completionRef = if (caller.isDefined) {
          // again, we can use Option#get here because we have just
          // checked to see if the caller is defined.
          (for {
            Caller(t) <- caller
            ((completionSymbol, completionIndex), completionSymbolParamClauseIndex) <- t.symbol.firstParamSyms
          } yield ref(completionSymbol).changeOwner(newSym, existsTree(t).get.owner)).get
        } else defaultContinuation
        val argsWithoutSuspend = args.filterNot(isSuspend)

        val finalApply = if (accumulator.isEmpty) {
          symRef.appliedTo(completionRef)
        } else if(argsWithoutSuspend.isEmpty) {
          val accumulatorHead = accumulator.head
          accumulator.drop(1).foldLeft((symRef.appliedToArgs(accumulatorHead), 1)){
            case ((currentApply, index), nextArgs) =>
              if(index == completionIndex) {
                val newApply = currentApply.appliedTo(completionRef)
                if(nextArgs.filterNot(isSuspend).isEmpty){
                  (newApply, index + 1)
                } else (newApply.appliedToArgs(nextArgs.filterNot(isSuspend)), index + 1)
              } else if(nextArgs.filterNot(isSuspend).isEmpty){
                (currentApply, index + 1)
              } else (currentApply.appliedToArgs(nextArgs.filterNot(isSuspend)), index + 1)
          }._1
        } else accumulator.foldLeft((symRef.appliedToArgs(argsWithoutSuspend), 0)){
            case ((currentApply, index), nextArgs) =>
              if(index == completionIndex) {
                val newApply = currentApply.appliedTo(completionRef)
                if(nextArgs.filterNot(isSuspend).isEmpty){
                  (newApply, index + 1)
                } else (newApply.appliedToArgs(nextArgs.filterNot(isSuspend)), index + 1)
              } else if(nextArgs.filterNot(isSuspend).isEmpty){
                (currentApply, index + 1)
              } else (currentApply.appliedToArgs(nextArgs.filterNot(isSuspend)), index + 1)
        }._1
        // println(s"finalApply: ${finalApply.show}")
        finalApply
    }

  /**
   * In this override, we transform the apply trees that have to be transformed to contain a new
   * completion continuation parameter.
   *
   * @param tree
   *   The apply tree to transform
   * @return
   *   The new GenericApply tree with the correct completion param and all non-Suspend type args
   *   in the correct param positions.
   */
  override def transformDefDef(defDefTree: tpd.DefDef)(using Context): tpd.Tree =
    println("transformDefDef")
    TreeTypeMap(
      treeMap = {
        case tree @ Apply(fn, args) if applyToChange.exists(_.sameTree(tree)) =>
          println(s"will change args to nearest completion or default: ${tree}")
          val replacement = deconstructNestedApply(tree, Nil)
          replacement
        case tree @ Apply(fn, args) if args.exists(isSuspend) =>
          println(s"will deconstruct suspended arg: ${tree}")
          val replacement = deconstructNestedApply(tree, Nil)
          replacement
        case tree @ Apply(_, _) =>
          println(s"unchanged apply: ${tree}")
          tree
        case t => t
      }
    )(defDefTree)

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"

  /**
   * A unique value class for holding the caller defdef of a suspended definition apply. Knowing
   * the caller allows us to choose the default completion or the caller's completion parameter.
   *
   * @param t
   *   The caller
   */
  case class Caller(t: Tree)

  /**
   * A dotty Property.Key[V] class to use to add attachments to Apply trees that contain the
   * closest caller to a suspended definition.
   */
  case object CallerKey extends Key[Caller]
}
