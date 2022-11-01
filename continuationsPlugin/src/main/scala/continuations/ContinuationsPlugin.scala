package continuations

import dotty.tools.dotc.ast.Trees.*
import dotty.tools.dotc.ast.{tpd, Trees}
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.StdNames.*
import dotty.tools.dotc.core.Symbols.{Symbol, TermSymbol, *}
import dotty.tools.dotc.core.Types.{AppliedType, Type}
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import dotty.tools.dotc.report
import dotty.tools.dotc.transform.{PickleQuotes, Staging}

import scala.annotation.tailrec

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
//    //val state = suspensionState(tree)
//    report.error("DEEP FOLD")
//    tree.deepFold(())((acc, tree) => {
//      report.error(tree.show)
//      acc
//    })
//    report.error("SHALLOW FOLD")
//    tree.shallowFold(())((acc, tree) => {
//      report.error(tree.show)
//      acc
//    })
    // tree

    report.logWith("transformBlock")(tree)
    report.logWith("transformBlock")(tree.show)
    tree

  var continuationTraitSym: ClassSymbol = _
  var continuationObjectSym: TermSymbol = _
  var safeContinuationClassApplySym: TermSymbol = _
  var interceptedMethodSym: TermSymbol = _

  override def prepareForUnit(tree: Tree)(using Context): Context =
    continuationTraitSym = requiredClass("continuations.Continuation")
    continuationObjectSym = requiredModule("continuations.Continuation")

    safeContinuationClassApplySym = requiredModule("continuations.SafeContinuation")
      .requiredMethod("apply", List(defn.AnyType, defn.AnyType))

    interceptedMethodSym =
      requiredPackage("continuations.intrinsics").requiredMethod("intercepted")

    ctx

  override def transformDefDef(tree: DefDef)(using Context): Tree = {
    val (input, callsSuspendContinuation): (Tree, Boolean) =
      tree.rhs match
        case Inlined(
              Apply(
                Apply(
                  fun1,
                  List(
                    Block(
                      Nil,
                      Block(List(DefDef(_, _, _, Block(Nil, Apply(fun2, List(arg))))), _)))),
                List(_)),
              Nil,
              _) =>
          (
            arg,
            fun1.symbol.showFullName == "continuations.Continuation.suspendContinuation" &&
              fun2.symbol.showFullName == "continuations.Continuation.resume"
          )
        case _ =>
          (EmptyTree, false)

    tree.termParamss match
      case List(Nil, usingSuspend :: Nil)
          if isSuspendType(usingSuspend.tpe) && callsSuspendContinuation =>
        val parent: Symbol = tree.symbol
        val returnType: Trees.Tree[Type] = tree.tpt

        // Continuation[Int]
        val continuationTyped: AppliedTypeTree =
          AppliedTypeTree(ref(continuationTraitSym), List(returnType))

        // (completion: continuations.Continuation[Int])
        val completion =
          newSymbol(parent, termName("completion"), Flags.LocalParam, continuationTyped.tpe)

        // val continuation1: continuations.Continuation = completion
        val continuation1: ValDef =
          ValDef(
            newSymbol(parent, termName("continuation1"), Flags.Local, continuationTyped.tpe),
            ref(completion))

        // Continuation.State.Undecided
        val undecided =
          ref(continuationObjectSym).select(termName("State")).select(termName("Undecided"))

        // continuation1.intercepted()
        def interceptedCall =
          ref(interceptedMethodSym)
            .appliedToType(returnType.tpe)
            .appliedTo(ref(continuation1.symbol))
            .appliedToNone

        // SafeContinuation(continuation1.intercepted(), Continuation.State.Undecided)
        // TODO: how to call it with `new`, is this why it fails?
        val safeContinuationConstructor =
          ref(safeContinuationClassApplySym)
            .appliedToType(returnType.tpe)
            .appliedTo(interceptedCall, undecided)

        // val safeContinuation: SafeContinuation[Int] = SafeContinuation(continuation1.intercepted(), Continuation.State.Undecided)
        val safeContinuation: ValDef =
          ValDef(
            newSymbol(
              parent,
              termName("safeContinuation"),
              Flags.Local,
              safeContinuationConstructor.tpe),
            safeContinuationConstructor)

        // val suspendContinuation: Int = 0
        // TODO: try decompile with foo: String, see if it is the same
        val suspendContinuation: ValDef =
          ValDef(
            newSymbol(
              parent,
              termName("suspendContinuation"),
              Flags.Local,
              ctx.definitions.IntType),
            Literal(Constant(0))
          )

        // safeContinuation.resume(Right(Int.box(1)))
        // Int.box should happen from the 1st task in the ClickUp ticket
        val suspendContinuationResume =
          safeContinuation.tpt.select(termName("resume")).appliedTo(input)

        // safeContinuation.getOrThrow()
        val suspendContinuationGetThrow =
          safeContinuation.tpt.select(termName("getOrThrow")).appliedToNone

        val body = Block(
          continuation1 :: safeContinuation :: suspendContinuation :: suspendContinuationResume :: Nil,
          suspendContinuationGetThrow
        )

        val methodDef =
          DefDef(parent.asTerm, List(List(completion)), ctx.definitions.AnyType, body)

        println(s"PLUGIN ${methodDef.show}")

        methodDef
      case _ =>
        tree
  }

  @tailrec final def transformStatements(
      block: Block,
      statements: List[Tree],
      previous: List[Tree])(using ctx: Context): Block =
    statements match
      case Nil => block
      case current :: remaining =>
        if (hasInnerSuspensionPoint(current))
          val newBlock = Block(???, ???)
          transformStatements(newBlock, remaining, Nil)
        // TODO nest previous under suspension point. Look at trees dif with example function
        else transformStatements(block, remaining, previous :+ current) // this may be wrong

  def isSuspendType(tpe: Type)(using ctx: Context): Boolean =
    tpe.classSymbol.showFullName == "continuations.Suspend"

  def returnsContextFunctionWithSuspendType(tree: Tree)(using ctx: Context): Boolean =
    ctx.definitions.isContextFunctionType(tree.tpe) && tree.tpe.argTypes.exists(isSuspendType)

  def hasInnerSuspensionPoint(statement: Tree)(using ctx: Context): Boolean =
    statement.find(isCallToSuspend).isDefined

  def isCallToSuspend(tree: Tree)(using ctx: Context): Boolean =
    tree match
      case Apply(_, _) => returnsContextFunctionWithSuspendType(tree)
      case _ => false
