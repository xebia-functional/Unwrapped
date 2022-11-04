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
import dotty.tools.dotc.core.Types.{AppliedType, OrType, Type}
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import dotty.tools.dotc.report
import dotty.tools.dotc.transform.{PickleQuotes, Staging}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

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

  // override def transformBlock(tree: Block)(using ctx: Context): Tree =
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

    // report.logWith("transformBlock")(tree)
    // report.logWith("transformBlock")(tree.show)
    // tree

  var continuationTraitSym: ClassSymbol = _
  var continuationObjectSym: Symbol = _
  var safeContinuationClassSym: ClassSymbol = _
  var interceptedMethodSym: TermSymbol = _

  override def prepareForUnit(tree: Tree)(using Context): Context =
    continuationTraitSym = requiredClass("continuations.Continuation")
    continuationObjectSym = continuationTraitSym.companionModule

    safeContinuationClassSym = requiredClass("continuations.SafeContinuation")

    interceptedMethodSym =
      requiredPackage("continuations.intrinsics").requiredMethod("intercepted")

    ctx

  override def transformDefDef(tree: DefDef)(using Context): Tree =
    transformSuspendNoParametersOneContinuationResume(tree)

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

  def hasOnlySuspendParam(tree: DefDef)(using Context): Boolean =
    tree.termParamss match
      case List(Nil, param :: Nil) => isSuspendType(param.tpe)
      case _ => false

  def getAllSubTrees(tree: Tree)(using Context): List[Tree] = {
    val deepFolder: DeepFolder[ListBuffer[tpd.Tree]] =
      new DeepFolder[ListBuffer[Tree]]((subTrees, tree) => subTrees += tree)

    /*
     * Needed for the `case Inlined(...)` because `DeepFolder.apply(...).foldOver` for the `Inlined` case  doesn't use
     * the `call` tree and as a result the `Inlined` is not being unwrapped.
     */
    @tailrec
    def recurse(trees: List[Tree], buf: ListBuffer[Tree]): List[Tree] =
      trees match
        case Nil =>
          buf.toList
        case Inlined(call, _, _) :: rest =>
          recurse(call :: rest, buf)
        case t :: rest =>
          val (inlined, notInlined) =
            deepFolder.apply(ListBuffer[Tree](), t).partition {
              case _: Inlined => true
              case _ => false
            }
          recurse(inlined.toList ++ rest, notInlined ++ buf)

    recurse(tree.toList, ListBuffer[Tree]())
  }

  /*
   * It works with only one `suspendContinuation` that just calls `resume`
   * and it replaces the whole parent method body
   */
  def transformSuspendNoParametersOneContinuationResume(tree: DefDef)(using Context): Tree = {
    if (hasOnlySuspendParam(tree)) {

      val suspendContinuationResumeCall: List[Tree] =
        getAllSubTrees(tree.rhs)
          .collect {
            case Apply(TypeApply(fun, _), List(arg))
                if fun
                  .symbol
                  .showFullName == "continuations.Continuation.suspendContinuation" =>
              arg
          }
          .flatMap(getAllSubTrees)
          .collect {
            case Apply(fun, List(arg))
                if fun.symbol.showFullName == "continuations.Continuation.resume" =>
              arg
          }

      if (suspendContinuationResumeCall.size == 1) {
        val parent: Symbol = tree.symbol
        val returnType: Type = tree.tpt.tpe
        val resumeInput = suspendContinuationResumeCall.head

        val continuationTyped: Type =
          continuationTraitSym.typeRef.appliedTo(returnType)

        val completion =
          newSymbol(parent, termName("completion"), Flags.LocalParam, continuationTyped)

        val continuation1: ValDef =
          ValDef(
            newSymbol(parent, termName("continuation1"), Flags.Local, continuationTyped),
            ref(completion))

        val undecidedState =
          ref(continuationObjectSym).select(termName("State")).select(termName("Undecided"))

        val interceptedCall =
          ref(interceptedMethodSym)
            .appliedToType(returnType)
            .appliedTo(ref(continuation1.symbol))
            .appliedToNone

        val safeContinuationConstructor =
          New(ref(safeContinuationClassSym))
            .select(nme.CONSTRUCTOR)
            .appliedToType(returnType)
            .appliedTo(interceptedCall, undecidedState)

        val safeContinuation: ValDef =
          ValDef(
            newSymbol(
              parent,
              termName("safeContinuation"),
              Flags.Local,
              safeContinuationConstructor.tpe),
            safeContinuationConstructor)

        val safeContinuationRef =
          ref(safeContinuation.symbol)

        val suspendContinuation: ValDef =
          ValDef(
            newSymbol(
              parent,
              termName("suspendContinuation"),
              Flags.Local,
              ctx.definitions.IntType),
            Literal(Constant(0))
          )

        val suspendContinuationResume =
          safeContinuationRef.select(termName("resume")).appliedTo(resumeInput)

        val suspendContinuationGetThrow =
          safeContinuationRef.select(termName("getOrThrow")).appliedToNone

        val body = Block(
          continuation1 :: safeContinuation :: suspendContinuation :: suspendContinuationResume :: Nil,
          suspendContinuationGetThrow
        )

        val suspendedState =
          ref(continuationObjectSym).select(termName("State")).select(termName("Suspended"))

        /* TODO:
         * Do we need to handle and remove `Continuation.State.Suspended.type` from this return type?
         *
         * If `Suspended` is actually being returned without being handled it will result to a `ClassCastException` as
         * in runtime we have a method which expects a value that is of `returnType`.
         */
        // Any | Null | Continuation.State.Suspended.type
        val finalMethodReturnType =
          OrType(
            OrType(ctx.definitions.AnyType, ctx.definitions.NullType, soft = false),
            suspendedState.symbol.namedType,
            soft = false)

        DefDef(parent.asTerm, List(List(completion)), finalMethodReturnType, body)
      } else tree
    } else tree
  }
