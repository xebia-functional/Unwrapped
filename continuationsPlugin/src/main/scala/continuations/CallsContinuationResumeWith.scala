package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Names.*

/**
 * Matcher for detecting methods that call and [[continuations.Suspend#suspendContinuation]]
 * [[continuations.Continuation.resume
 */
private[continuations] object CallsContinuationResumeWith:

  /**
   * @param tree
   *   the [[dotty.tools.dotc.ast.tpd.Tree]] to match upon
   * @return
   *   [[scala.Some]] if the tree contains a subtree call to
   *   [[continuations.Suspend#suspendContinuation]] and [[continuations.Continuation.resume]],
   *   [[scala.None]] otherwise
   */
  def unapply(tree: DefDef)(using Context): Option[Tree] =
    val args =
      tree
        .rhs
        .filterSubTrees(_.denot.matches(suspendContinuationMethod.symbol))
        .flatMap {
          case Apply(_, List(Block(Nil, Block(List(DefDef(_, _, _, suspendBody)), _)))) =>
            Option(suspendBody)
          case Apply(_, List(Block(List(DefDef(_, _, _, suspendBody)), _))) =>
            Option(suspendBody)
          case _ =>
            None
        }
        .flatMap {
          case Block(Nil, Apply(fun, List(arg))) if treeIsContinuationsResume(fun) =>
            Option(arg.withType(arg.tpe))
          case Apply(fun, List(arg)) if treeIsContinuationsResume(fun) =>
            Option(arg.withType(arg.tpe))
          case _ =>
            None
        }

    Option.when(args.size == 1)(args.head)
