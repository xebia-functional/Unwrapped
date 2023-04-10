package continuations

import dotty.tools.dotc.ast.Trees.ParamClause
import dotty.tools.dotc.ast.{TreeTypeMap, Trees, tpd}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.ast.untpd.Modifiers
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{Context, ctx}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Types}
import dotty.tools.dotc.core.Names.{Name, termName, typeName}
import dotty.tools.dotc.core.StdNames.{nme, tpnme}
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType}
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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
    DefDefTransforms.transformSuspendContinuation(tree)

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

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

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[Tree] = ListBuffer.empty

  private def existsTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find { s =>
      tree.existsSubTree(t => s.name == t.symbol.name && s.coord == t.symbol.coord)
    }

  private def findTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  private def treeIsSuspendAndNotInApplyToChange(tree: Apply)(using Context): Boolean =
    tree.filterSubTrees(CallsSuspendParameter.apply).nonEmpty
      // && !applyToChange.exists(_.filterSubTrees(_.sameTree(tree)).nonEmpty)

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

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    val hasContinuationParam =
      tree.termParamss.flatten.exists { p =>
        !p.symbol.is(Flags.Given) &&
        p.tpt.tpe.matches(requiredClassRef(continuationFullName)) &&
        p.name.toString == completionParamName
      }

    if (hasContinuationParam) updatedMethods.addOne(tree.symbol)

    ctx

  override def prepareForApply(tree: Apply)(using Context): Context =
    tree match
      case Apply(Apply(_, _), _)
          if existsTree(tree).nonEmpty && treeIsSuspendAndNotInApplyToChange(tree) =>
        applyToChange.addOne(tree)
      case Apply(Select(_, selected), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(tree, selected) =>
        applyToChange.addOne(tree)
      case Apply(TypeApply(Select(_, selected), _), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(tree, selected) =>
        applyToChange.addOne(tree)
      case Apply(Ident(_), _)
          if findTree(tree).nonEmpty && treeIsSuspendAndNotInApplyToChange(tree) =>
        applyToChange.addOne(tree)
      case _ =>
        ()
    ctx

  override def transformApply(tree: Apply)(using ctx: Context): Tree =

    if (tree.symbol.showFullName == "continuations.jvm.internal.SuspendApp.apply")

      val continuationClassRef = requiredClassRef("continuations.Continuation")
      val thingClassRef = requiredClassRef("continuations.jvm.internal.Thing")

      val anonFun = tree.filterSubTrees(st => st.symbol.showName == "$anonfun") // TODO

      import dotty.tools.dotc.core.Decorators.toTermName
      val message1 = "inside invoke\n"
      val consoleType1 = requiredModule("scala.Console")
      val consoleRef1 = ref(consoleType1)
      val print1 = consoleRef1.select("print".toTermName)
      val printMessage1 = print1.appliedTo(tpd.Literal(Constant(message1)))
      
      val thingClass = newCompleteClassSymbol(
        anonFun.head.symbol.owner,
        tpnme.ANON_CLASS,
        Flags.SyntheticArtifact | Flags.Private | Flags.Final,
        List(thingClassRef.classSymbol.typeRef),
        Scopes.newScope
      ).entered.asClass

      val constructor = {
        val symbol = newConstructor(
          thingClass,
          Flags.Synthetic,
          List.empty,
          List.empty
        ).entered.asTerm
        DefDef(symbol)
      }

      val invokeSymbol =
        newSymbol(
          thingClass, /* <-- module class SuspendApp$ anonFunSymbol.head.symbol.owner, */
          Names.termName("invoke"),
          Flags.Override | Flags.Method,
          MethodType(
            List(termName("completion")),
            List(continuationClassRef.appliedTo(defn.IntType)),
            Types.OrNull(OrType(defn.IntType, defn.AnyType, true))
          )
        ).entered.asTerm

      def createInvokeMethod(/*stats: List[Tree], expr: Tree*/): DefDef = tpd.DefDef(
        invokeSymbol,
        paramss =>

          val (paramsNonCF, _) =
            tree.deepFold((List(List.empty[Tree]), List(List.empty[Tree]))) {
              case (
                (accNonCF, accCF),
                Apply(TypeApply(Select(qualifier, selected), argsType), args))
                if treeExistsIsApplyAndIsMethod(qualifier, selected) =>
                (accNonCF, accCF.prepended(removeSuspend(args)).prepended(removeSuspend(argsType)))
              case ((accNonCF, accCF), Apply(Select(qualifier, selected), args))
                if treeExistsIsApplyAndIsMethod(qualifier, selected) =>
                (accNonCF, accCF.prepended(removeSuspend(args)))
              case ((accNonCF, accCF), Apply(fun, args)) if treeExistsAndIsMethod(fun) =>
                (accNonCF.prepended(removeSuspend(args)), accCF)
              case (acc, _) => acc
            }

          val substituteFunctionCall = new TreeTypeMap(
            treeMap = tree =>
              if applyToChange.exists(_.sameTree(tree)) then ref(existsTree(tree).get).appliedToTermArgs(paramsNonCF.flatten :+ paramss.head.head) else tree
          )

          // val newStats = substituteFunctionCall.transform(stats)

          /*
          Block(
            newStats,
            expr
          )
          */
          ref(existsTree(tree).get).appliedToTermArgs(paramsNonCF.flatten :+ paramss.head.head)
          
      )

      // deleteOldSymbol(tree.symbol)

      val substituteContinuationCall = new TreeTypeMap(
        treeMap = {
          /*
          case tree@Block(stats@List(DefDef(_, paramss, _, _)), expr@_) if tree.existsSubTree(st => applyToChange.exists(_.sameTree(st))) =>
          */
          case tree if applyToChange.exists(_.sameTree(tree)) =>

            val newClass = ClassDefWithParents(
              thingClass,
              constructor,
              List(tpd.New(thingClassRef)),
              List(createInvokeMethod())
            )

            val newClass2 = tpd
              .New(tpd.TypeTree(newClass.tpe))
              .select(nme.CONSTRUCTOR)

            Block(
              List(
                printMessage1,
                newClass
              ),
              newClass2
            )
          case tree =>
            tree
        }
      )

      val res = cpy.Apply(tree)(
        fun = tree.fun,
        args = substituteContinuationCall.transform(tree.args)
      )
      res

    else
      tree

end ContinuationsCallsPhase

object ContinuationsCallsPhase {
  val name = "continuationsCallsPhase"
}
