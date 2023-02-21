package continuations

import munit.SnapshotSuite
import scala.util.Try
import dotty.tools.dotc.core.Contexts.Context
import scala.concurrent.Promise
import dotty.tools.dotc.ast.tpd.Tree
import io.circe.generic.AutoDerivation

class ContinuationsPluginSnapshots extends SnapshotSuite, CompilerFixtures, AutoDerivation {
  compilerContextWithContinuationsPlugin.snapshotTest("Example") {
    case given Context =>
      val source =
        """| import continuations.*
           | def foo()(using Suspend): Int = 1""".stripMargin
      val p: Promise[(String, String)] = Promise[(String, String)]
      continuationsCompilerSnapshot(source)
  }
}
