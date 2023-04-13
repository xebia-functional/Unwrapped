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
import dotty.tools.dotc.ast.Trees.Tree
import dotty.tools.dotc.core.Types.Type
import scala.annotation.meta.param
import dotty.tools.dotc.core.Types.RefinedType

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

  override def transformValDef(tree: ValDef)(using Context): tpd.Tree =
    println(s"valOrDefDef.show: ${tree.show}")
    println(s"valOrDefDef.symbol.info.show: ${tree.symbol.info.show}")
    println(s"valOrDefDef.symbol.info: ${tree.symbol.info}")
    DefDefTransforms.transformSuspendContinuation(tree)

  override def transformDefDef(tree: DefDef)(using ctx: Context): tpd.Tree =
    println(s"valOrDefDef.show: ${tree.show}")
    println(s"valOrDefDef.symbol.info.show: ${tree.symbol.info.show}")
    println(s"valOrDefDef.symbol.info: ${tree.symbol.info}")
    try {
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

  def isSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.info.hasClassSymbol(requiredClass(suspendFullName))
  def isGivenSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.originalOwner.showFullName == suspendFullName

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[tpd.Tree] = ListBuffer.empty

  private def existsTree(tree: tpd.Tree)(using Context): Option[Symbol] =
    updatedMethods.find { s =>
      tree.existsSubTree(t =>
        s.name.show == t.symbol.name.show && s
          .paramSymss
          .exists(_.exists(_.info.hasClassSymbol(requiredClass(continuationFullName)))))
    }

  private def findTree(tree: tpd.Tree)(using Context): Option[Symbol] =
    updatedMethods.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  private def treeIsSuspendAndNotInApplyToChange(tree: Apply)(using Context): Boolean =
    tree.filterSubTrees(CallsSuspendParameter.apply).nonEmpty &&
      !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty)

  private def treeExistsIsApplyAndIsNotInApplyToChange(tree: Apply, n: Name)(
      using Context): Boolean =
    existsTree(tree).nonEmpty &&
      n.asTermName == nme.apply &&
      treeIsSuspendAndNotInApplyToChange(tree)

  private def removeSuspend(trees: List[tpd.Tree])(using Context): List[tpd.Tree] =
    trees.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))

  private def treeExistsAndIsMethod(tree: tpd.Tree)(using Context): Boolean =
    existsTree(tree).exists(_.is(Flags.Method))

  private def treeExistsIsApplyAndIsMethod(tree: tpd.Tree, n: Name)(using Context): Boolean =
    n.asTermName == nme.apply &&
      treeExistsAndIsMethod(tree)

  def hasContinuationParam(tree: DefDef)(using Context): Boolean =
    tree.termParamss.flatten.exists { p =>
      !p.symbol.is(Flags.Given) &&
      p.tpt.tpe.matches(requiredClassRef(continuationFullName))
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
    if (hasContinuationParam(tree))
      updatedMethods.addOne(tree.symbol)
    tree.foreachSubTree {
      case a @ Apply(_, _) if hasContinuationParam(tree) && tree.symbol.isAnonymousFunction =>
      case a @ Apply(_, _) if hasContinuationParam(tree) && tree.symbol.isAnonymousFunction =>
      case a @ Apply(_, _) if hasContinuationParam(tree) && !tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(_, _) if hasContinuationParam(tree) && !tree.symbol.isAnonymousFunction =>
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
      case t =>
        ()
    }
    ctx

  extension (s: Symbol)
    def firstParamSyms(using Context): Option[((Symbol, Int), Int)] = s
      .paramSymss
      .zipWithIndex
      .find(_._1.exists(_.info.hasClassSymbol(requiredClass(continuationFullName))))
      .flatMap(pc =>
        // reviewer request for comment -- do we have a isContinuation anywhere?
        pc._1
          .zipWithIndex
          .find(_._1.info.hasClassSymbol(requiredClass(continuationFullName)))
          .map(foundCompletionPc => (foundCompletionPc, pc._2)))

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
  def deconstructNestedApply(tree: Apply, accumulator: List[List[tpd.Tree]])(
      using Context): tpd.Tree =
    println("deconstructNestedApply")
    val defaultContinuation: tpd.Tree = ref(
      requiredModule("continuations.jvm.internal.ContinuationStub").requiredMethod("contImpl"))
    tree match {
      case Apply(Select(fn @ Apply(_, _), selected), args) =>
        println(s"deconstructNestedApply Select ${tree.show}")
        val newArgs = args.filterNot(isSuspend)
        val deconstructedInnerApply = deconstructNestedApply(fn, Nil)
        if (newArgs.isEmpty) deconstructedInnerApply
        else Select(deconstructNestedApply(fn, Nil), selected).appliedToTermArgs(newArgs)
      case Apply(b @ Apply(_, _), _) =>
        println(s"deconstructNestedApply Apply Apply ${tree.show}")
        deconstructNestedApply(b, tree.args :: accumulator)
      case Apply(fn @ TypeApply(_, tfnArgs), args) =>
        println(s"deconstructNestedApply 222 Apply ${tree.show}, ${accumulator}")
        println(s"deconstructNestedApply 223 Apply tree $tree")
        println(s"updatedMethods $updatedMethods")
        val newSym = existsTree(fn).get
        println(newSym.defTree)
        val symRef = ref(newSym)
        val caller = tree.getAttachment(CallerKey)
        // we can use Option#get in these calls because in
        // prepareForDefDef we have identified all the applies that
        // must change, so we know that the Options will not be None.
        val (_, completionIndex) = newSym.firstParamSyms.get
        println(completionIndex)

        val completionRef = if (caller.isDefined) {
          println("caller isDefined")
          // again, we can use Option#get here because we have just
          // checked to see if the caller is defined.
          (for {
            Caller(t) <- caller
            ((completionSymbol, completionIndex), completionSymbolParamClauseIndex) <- t
              .symbol
              .firstParamSyms
          } yield ref(completionSymbol).changeOwner(newSym, existsTree(t).get.owner)).get
        } else defaultContinuation

        val argsWithoutSuspend: List[Tree[Type]] = args.filterNot(isSuspend)

        val argsWithCompletion =
          (argsWithoutSuspend :: accumulator).insertAt(completionIndex, List(completionRef))

        val combinedParamClauses =
          if (newSym.paramSymss.exists(_.exists(_.isType))) {
            tfnArgs :: argsWithCompletion
          } else argsWithCompletion

        val filteredParamClauses =
          combinedParamClauses.map(_.filterNot(isSuspend)).filter(_.nonEmpty)

        println(s"filteredParamClauses: ${filteredParamClauses}")

        val finalApply = symRef.appliedToArgss(filteredParamClauses)
        println(s"final apply tree: $finalApply")
        println(s"final apply: ${finalApply.show}")
        finalApply
      case Apply(fn, args) =>
        println(s"deconstructNestedApply 292 Apply ${tree.show}")
        println(s"deconstructNestedApply 292 Apply tree $tree")
        val newSym = existsTree(fn).get
        val symRef = ref(newSym)
        val caller = tree.getAttachment(CallerKey)
        // we can use Option#get in these calls because in
        // prepareForDefDef we have identified all the applies that
        // must change, so we know that the Options will not be None.
        val (_, completionIndex) = newSym.firstParamSyms.get

        val completionRef = if (caller.isDefined) {
          println("caller isDefined")
          // again, we can use Option#get here because we have just
          // checked to see if the caller is defined.
          (for {
            Caller(t) <- caller
            _ = println(s"t: ${t.show}")
            ((completionSymbol, completionIndex), completionSymbolParamClauseIndex) <- t
              .symbol
              .firstParamSyms
            _ = println(s"completionSymbol: ${completionSymbol}")
            _ = println(s"completionIndex: ${completionIndex}")
          } yield ref(completionSymbol).changeOwner(newSym, existsTree(t).get.owner)).get
        } else defaultContinuation

        val argsWithoutSuspend: List[Tree[Type]] = args.filterNot(isSuspend)

        val combinedParamClauses =
          (argsWithoutSuspend :: accumulator).insertAt(completionIndex, List(completionRef))

        val filteredParamClauses =
          combinedParamClauses.map(_.filterNot(isSuspend)).filter(_.nonEmpty)

        val finalApply = symRef.appliedToArgss(filteredParamClauses)

        println(s"finalApply: ${finalApply}")
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
    TreeTypeMap(
      treeMap = {
        // case tree @ Apply(fn, args) if applyToChange.exists(_.sameTree(tree)) =>
        //   println(s"will change args to nearest completion or default: ${tree}")
        //   println(s"existsTree(fn): ${existsTree(fn)}")
        //   val possibleNewTree = TreeTypeMap(substTo = List(existsTree(fn).get), substFrom = List(fn.symbol))(tree)
        //   println(s"oldTree show: ${tree.show}")
        //   println(s"possibleNewTree: ${possibleNewTree.show}")
        //   val replacement = deconstructNestedApply(tree, Nil)
        //   replacement
        case tree @ Apply(fn, args) if args.exists(isSuspend) =>
          val replacement = deconstructNestedApply(tree, Nil)
          TreeTypeMap(
            substTo = List(existsTree(fn).get),
            substFrom = List(fn.symbol),
            newOwners = List(existsTree(fn).get),
            oldOwners = List(fn.symbol))(replacement)
        case tree @ Apply(_, _) =>
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
  case class Caller(t: tpd.Tree)

  /**
   * A dotty Property.Key[V] class to use to add attachments to Apply trees that contain the
   * closest caller to a suspended definition.
   */
  case object CallerKey extends Key[Caller]
}
