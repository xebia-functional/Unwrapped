package example

import fx.*
import munit.fx.ScalaFXSuite

class ScalaFXSuiteIntegrationSuite extends ScalaFXSuite:
  def myFunction(
      value1: Either[String, Int],
      value2: Option[Int]): Raise[String | None.type] ?=> Int =
    value1.bind + value2.bind

  testFX("The computation's result satisfies the assertion") {
    assertFX(myFunction(Right(1), Option(2)) == 3)
  }

  testFX("The computation's result doesn't satisfy the assertion".fail) {
    assertFX(myFunction(Right(1), Option(2)) == 0)
  }

  testFX("The computation is short circuited with a None".fail) {
    assertFX(myFunction(Right(1), None) == 1)
  }

  testFX("The computation is short circuited with a Left".fail) {
    assertFX(myFunction(Left("BOOM!"), Option(2)) == 2)
  }

  testFX("Example 1".fail) {
    assertFX(false)
  }
  testFX("Example 2") {
    val x: Console ?=> Unit = "example 1".writeLine()
    val y: Console ?=> Unit = "example 2".writeLine()
    structured(
      parallel(
        () => {
          x
          assertFX(true)
        },
        () => {
          y
          assertFX(true)
        }
      )
    )
  }

  fixture.testFX("Fixtures should work as well") { int =>
    assertEqualsDouble(int * 3.00, int * 3.00, 0.00)
  }

  fixture.testFX("Fixtures should work with options, too".fail) { int =>
    assertEqualsDouble(int * 3.00, int, 0.00)
  }

  fixturePair.testFX("Pair fixtures also work") { pair => assertEquals(pair._2, pair._2) }

  fixturePair.testFX("Pair fixtures also work with options".fail) { pair =>
    assertEquals(pair._2, "doesn't match the pair")
  }

  fixtureTriple.testFX("Triple fixtures also work") { pair => assertEquals(pair._3, pair._3) }

  fixtureTriple.testFX("Triple fixtures also work with options".fail) { pair =>
    assertEquals(pair._3, 'c')
  }

  lazy val fixture = FunFixture(setup = testOptions => 1, teardown = intNum => ())

  lazy val fixtureString = FunFixture(setup = options => options.name, str => ())

  lazy val fixtureChar = FunFixture(setup = options => options.name.head, char => ())

  lazy val fixturePair = FunFixture.map2(fixture, fixtureString)

  lazy val fixtureTriple = FunFixture.map3(fixture, fixtureString, fixtureChar)
