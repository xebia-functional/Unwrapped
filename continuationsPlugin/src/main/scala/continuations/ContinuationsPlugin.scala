package continuations

import dotty.tools.dotc.ast.Trees.ParamClause
import dotty.tools.dotc.ast.{tpd, TreeTypeMap, Trees}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.ast.untpd.Modifiers
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Types}
import dotty.tools.dotc.core.Names.{termName, typeName, Name}
import dotty.tools.dotc.core.StdNames.{nme, tpnme}
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType, PolyType}
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import dotty.tools.dotc.util.Property.Key
import continuations.ContinuationsCallsPhase.CallerKey
import continuations.ContinuationsCallsPhase.Caller
import dotty.tools.dotc.util.Property

class ContinuationsPlugin extends StandardPlugin:

  val name: String = "continuations"
  override val description: String = "CPS transformations"

  def init(options: List[String]): List[PluginPhase] =
    (new ContinuationsPhase) :: new ContinuationsCallsPhase :: new ContinuationsAfterPhase :: Nil

class ContinuationsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsPhase.name

  override def changesBaseTypes: Boolean = true

  override def changesMembers: Boolean = true

  override val runsAfter = Set(Staging.name)
  override val runsBefore = Set(ContinuationsCallsPhase.name)

  override def transformValDef(tree: ValDef)(using Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

end ContinuationsPhase

object ContinuationsPhase:
  val name = "continuations"

  /**
   * A unique value type for holding the method undergoing transformation.
   *
   * @param t The method undergoing transformation. Used in
   *   attachments to assist in the transformation.
   */
  case class TransformedMethod(t: tpd.Tree)

  /**
   * A dotty Property.Key[V] class to use to add suspended DefDef
   * attachments to trees during transformation.
   */
  case object TransformedMethodKey extends Key[TransformedMethod]

  /**
    * A unique value type for holding valdefs with suspension points
    * undergoing transformation.
    *
    * @param t The valdef containing the suspension point. Used in
    * attatchments to assist in the transformation.
    */
  case class SuspensionPointValDef(t: tpd.ValDef)

  /**
    * A dotty Property.Key[V] class used to add suspension point
    * attachments to trees during transformation.
    */
  case object SuspensionPointValDefKey extends Key[SuspensionPointValDef]

  /**
    * A unique value type for holding the label number to jump to in
    * suspension points undergoing transformation.
    *
    * @param i The number of the label to jump to.
    */
  case class JumpToLabelNumber(i: Int)

  /**
    * A dotty Property.Key[V] class used to add jump to label
    * attachments to trees during transformation.
    */
  case object JumpToLabelNumberKey extends Key[JumpToLabelNumber]

  /**
    * The labels created during a suspended transformation.
    *
    * @param labels The jump lables produced during a suspend
    * transformation
    */
  case class JumpLabels(labels: Vector[Symbol])

  /**
    * A dotty Property.Key[V] class used to add the labels generated
    * during suspend transformation to the suspended tree. Used in the
    * state machine case defs to drive transition to the next frame
    * counter state.
    */
  case object JumpLabelsKey extends Key[JumpLabels]


  /**
    * The main continuation match value generated during a suspended
    * transformation. Used in several places to construct the state
    * machine and state machine case def case exit statements used to
    * transition the state machine to the next state.
    *
    * @param vd The valdef of the continuation match value.
    */
  case class ContinuationValMatchVal(vd: tpd.ValDef)

  /**
    * A dotty Property.Key[V] class used to attach the main continuation
    * match value during suspend transformations.
    */
  case object ContinuationValMatchValKey extends Key[ContinuationValMatchVal] 

  /**
    * A dotty Property.Key[V] class used to attach the captured
    * continuation parameter during the transformation of inlined
    * suspend calls.
    */
  case object InlinedContinuationParameterKey extends Key[InlinedContinuationParameter]

  /**
    * An inlined Suspend#shift call's anonymous continuation parameter
    * captured during a suspended transformation. Used to construct
    * the SafeContinuation#resume and SafeContinuation#raise apply
    * trees that hook the suspension into the passed frame and the
    * frame's delegation to allow for interception, transformation,
    * and resumption of the continuation.
    *
    * @param vd The valdef containing the captured continuation
    * parameter.
    */
  case class InlinedContinuationParameter(vd: tpd.ValDef)


  /**
    * A safe continuation val that can be used to call the completion
    * in an inlined shift call.
    *
    * @param kd The suspension val def
    */
  case class SafeContinuationVal(kd: tpd.ValDef)

  /**
    * The attatchment key used to attatch the safe continuation to the
    * subtrees of an inlined call.
    */
  case object SafeContinuationValKey extends Key[SafeContinuationVal]

end ContinuationsPhase

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
  // override val runsBefore = Set(PickleQuotes.name)

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[Tree] = ListBuffer.empty

  private def existsTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find { s =>
      tree.existsSubTree(t => s.name == t.symbol.name && s.coord == t.symbol.coord)
    }

  private def findTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  private def treeIsSuspendAndNotInApplyToChange(tree: Apply)(using Context): Boolean =
    tree.filterSubTrees(CallsSuspendParameter.apply).nonEmpty && !applyToChange.exists(
      _.filterSubTrees(_.sameTree(tree)).nonEmpty)

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
    n.asTermName == nme.apply && treeExistsAndIsMethod(tree)

  def hasContinuationParam(tree: DefDef)(using Context): Boolean =
    tree.termParamss.flatten.exists { p =>
      p.tpt.tpe.matches(requiredClassRef(continuationFullName))
    }

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    if (hasContinuationParam(tree))
      updatedMethods.addOne(tree.symbol)
    tree.foreachSubTree {
      case a @ Apply(_, _) if hasContinuationParam(tree) && tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(_, _) if hasContinuationParam(tree) && tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
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
    summon[Context]
  end prepareForDefDef

  def isSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.info.hasClassSymbol(requiredClass(suspendFullName))

  def isGivenSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.originalOwner.showFullName == suspendFullName

  override def transformDefDef(tree: tpd.DefDef)(using Context): tpd.Tree =
    TreeTypeMap(
      treeMap = {
        case tree @ Apply(fn, args) if args.exists(isSuspend) =>
          val caller = tree.getAttachment(CallerKey).flatMap { caller =>
            caller
              .t
              .asInstanceOf[tpd.DefDef]
              .paramss
              .flatten
              .find(_.symbol.info.hasClassSymbol(requiredClass(continuationFullName)))
              .map(_.symbol)
          }
          println(s"caller: ${caller}")
          println(s"existsTree(fn).get: ${existsTree(fn).get}")
          TreeTypeMap(
            substTo = List(existsTree(fn).get) ++ caller.toList,
            substFrom = List(fn.symbol) ++ args
              .find(_.symbol.info.hasClassSymbol(requiredClass(suspendFullName)))
              .map(_.symbol)
              .toList,
            newOwners = existsTree(fn).toList ++ caller.toList,
            oldOwners = List(fn.symbol) ++ args
              .find(_.symbol.info.hasClassSymbol(requiredClass(suspendFullName)))
              .map(_.symbol.owner)
              .toList
          )(tpd.Apply(fn, args.map{ arg => if(isSuspend(arg)){ ref(caller.get) } else arg }))
        case tree @ Apply(_, _) =>
          tree
        case t => t
      }
    )(tree)
  end transformDefDef

end ContinuationsCallsPhase

object ContinuationsCallsPhase:
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


class ContinuationsAfterPhase extends PluginPhase:

  override def phaseName: String = ContinuationsAfterPhase.name

  override val runsAfter = Set(ContinuationsCallsPhase.name)

  override val runsBefore = Set(PickleQuotes.name)

  override def prepareForSelect(tree: tpd.Select)(using Context): Context =
    if(tree.qualifier.symbol.name.show == "safeContinuation"){
      println(s"tree.symbol.ownersIterator.exists: ${tree.qualifier.symbol.ownersIterator.toList.map(_.exists)} ${tree.qualifier.symbol.ownersIterator.toList.map(_.show)}")
      val twoArgumentsTwoContinuations = tree.qualifier.symbol.ownersIterator.find(_.name.show == "twoArgumentsTwoContinuations")
      println(s"twoArgumentsTwoContinuations type: ${twoArgumentsTwoContinuations.get.info.show}")
      println(s"twoArgumentsTwoContinuations type: ${twoArgumentsTwoContinuations.get.denot.exists}")
    }
    
    summon[Context]

  // override def prepareForApply(tree: tpd.Apply)(using Context): Context =
  //   if(tree.fun.symbol.name.show == resumeMethodName && tree.fun
  //             .symbol
  //             .owner
  //             .info
  //             .hasClassSymbol(requiredClass(safeContinuationClassName))){
  //     println(s"After continuations ${tree.symbol.show} tree.symbol.ownersIterator.toList.map(_.exists) ${tree.symbol.ownersIterator.toList.map(_.exists)} ${tree.symbol.ownersIterator.toList.map(_.show)}")
    // summon[Context]
    
end ContinuationsAfterPhase

object ContinuationsAfterPhase:
    val name: String = "continuationsAfterPhase"
end ContinuationsAfterPhase
