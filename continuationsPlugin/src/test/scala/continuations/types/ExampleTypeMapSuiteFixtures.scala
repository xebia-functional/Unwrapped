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

trait ExampleTypeMapSuiteFixtures { self: FunSuite with CompilerFixtures =>
  val symbol: FunFixture[
    Context ?=> (Symbol, Name, FlagSet, Type, Option[Symbol], Option[Coord]) => Symbol] =
    FunFixture(
      setup = _ =>
        (owner, name, flags, info, maybePrivateWithin, maybeCoord) => {
          val privateWithin = maybePrivateWithin.getOrElse(NoSymbol)
          val coord = maybeCoord.getOrElse(NoCoord)
          newSymbol(owner, name, flags, info, privateWithin, coord)
        },
      teardown = _ => ()
    )
  val methodType: FunFixture[Context ?=> (List[List[Symbol]], Type) => Type] =
    FunFixture(
      setup = _ =>
        (paramSymss, resultType) =>
          if (paramSymss.isEmpty) NoType
          else paramSymss.foldRight(resultType)(MethodType.fromSymbols),
      teardown = _ => ()
    )
  val symbolMakerAndMethodTypeMakerAndCompiler = FunFixture.map3(
    symbol,
    methodType,
    compilerContextWithContinuationsPlugin.asInstanceOf[FunFixture[Context]])
}
