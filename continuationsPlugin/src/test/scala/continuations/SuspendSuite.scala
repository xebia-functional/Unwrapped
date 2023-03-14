package continuations

import munit.FunSuite

class SuspendSuite extends FunSuite {
  test("shift should throw a CompilerRewriteUnsuccessfulException if it is not converted") {
    def foo()(using Suspend): Int =
      summon[Suspend].shift[Int] { _ => () }

    intercept[Suspend.CompilerRewriteUnsuccessfulException.type](foo())
  }
}
