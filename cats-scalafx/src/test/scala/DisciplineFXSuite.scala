package munit
package fx

import _root_.fx.Structured
import munit.fx.ScalaFXSuite
import munit.DisciplineSuite
import munit.Location
import org.typelevel.discipline.Laws

import scala.reflect.Typeable

abstract class DisciplineFXSuite extends ScalaFXSuite, DisciplineSuite {

  def checkAllLaws[R, F <: AssertionError: Typeable](
      name: String)(ruleSet: Laws#RuleSet): Location ?=> Unit =
    testFX(name) {
      checkAll(name, ruleSet)
    }

}
