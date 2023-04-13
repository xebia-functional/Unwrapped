package continuations
package types

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.Names.Name
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.MethodType
import dotty.tools.dotc.core.Types.NoType
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.util.Spans.Coord
import dotty.tools.dotc.util.Spans.NoCoord
import munit.FunSuite
import munit.FunFixtures

trait TypeMapSuiteFixtures { self: FunSuite with CompilerFixtures =>
  val methodType: FunFixture[Context ?=> (List[List[Type]], Type) => Type] =
    FunFixture(
      setup = _ =>
        (paramSymss, resultType) =>
          if (paramSymss.isEmpty) NoType
          else paramSymss.foldRight(resultType)(MethodType.apply),
      teardown = _ => ()
    )
  val methodTypeMakerAndCompiler = FunFixture.map2(
    methodType,
    compilerContextWithContinuationsPlugin.asInstanceOf[FunFixture[Context]])
}
