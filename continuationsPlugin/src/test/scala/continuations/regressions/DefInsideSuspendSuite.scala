package continuations
package regressions

import munit.FunSuite
import dotty.tools.dotc.core.Contexts.Context

class DefInsideSuspendSuite extends FunSuite, CompilerFixtures, DefInsideSuspendSuiteFixtures {

  defInsideSuspendFixtures.test(
    "The compiler plugin should not delete a normal def defined inside a suspend.") {
    case (given Context, source, expected) =>
      checkContinuations(source) {
        case (tree, _) =>
          assertNoDiff(cleanCompilerOutput(tree), expected)
      }
  }

}
