package continuations

import continuations.Types.flattenTypes
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class TypesSuite extends FunSuite, CompilerFixtures {

  compilerContextWithContinuationsPlugin.test(
    """flattenTypes(Int => String ?=> Int => (Boolean, Int) => Int ?=> Int) should be
      |List(
      | Int => (String) ?=> Int => (Boolean, Int) => (Int) ?=> Int,
      | (String) ?=> Int => (Boolean, Int) => (Int) ?=> Int,
      | Int => (Boolean, Int) => (Int) ?=> Int,
      | (Boolean, Int) => (Int) ?=> Int,
      | (Int) ?=> Int,
      | Int
      |)
      |""".stripMargin) {
    case given Context =>
      val type0 = defn.IntType
      val type1 = defn.FunctionOf(List(defn.IntType), type0, isContextual = true)
      val type2 = defn.FunctionOf(List(defn.BooleanType, defn.IntType), type1)
      val type3 = defn.FunctionOf(List(defn.IntType), type2)
      val type4 = defn.FunctionOf(List(defn.StringType), type3, isContextual = true)
      val type5 = defn.FunctionOf(List(defn.IntType), type4)

      assertEquals(
        flattenTypes(type5),
        List(
          type5,
          type4,
          type3,
          type2,
          type1,
          type0
        ))
  }
}
