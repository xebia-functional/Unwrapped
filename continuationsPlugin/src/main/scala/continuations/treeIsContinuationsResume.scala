package continuations

import dotty.tools.dotc.ast.Trees.{Tree => TTree}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*

/**
 * @param tree
 * @return
 *   True if the owner of the tree is a reference to the resume method of an instance of
 *   [[continuations.Continuation]]
 */
private[continuations] def treeIsContinuationsResume[A](tree: TTree[A])(
    using Context): Boolean =
  tree.symbol.owner.denot.matches(requiredClass(continuationFullName)) && tree
    .denot
    .name
    .show == ref(requiredMethod(resumeFullName)).denot.name.show
