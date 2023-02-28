package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.MethodType
import munit.FunSuite

class FlattenBlockSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndFlattenableNestedBlock.test("It should flatten nested blocks") {
    case (given Context, sourceBlock) =>
      val sourceTree =
        DefDefTransforms.flattenBlock(sourceBlock).show
      val expectedTree = Block(List(), Literal(Constant(1))).show
      assertNoDiff(sourceTree, expectedTree)
  }

  continuationsContextAndUnflattenableNestedBlock.test(
    "It should not flatten nested closure blocks") {
    case (given Context, sourceBlock) =>
      val expectedTree = Block(
        List(
          Lambda(
            MethodType.apply(List(defn.ThrowableType))(_ => defn.NothingType),
            trees => Throw(trees.head))),
        Literal(Constant(1))).show
      val sourceTree =
        DefDefTransforms.flattenBlock(sourceBlock).show
      assertNoDiff(sourceTree, expectedTree)
  }

}
