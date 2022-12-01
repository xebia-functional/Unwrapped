package continuations

import munit.FunSuite

class SuspendSuite extends FunSuite {
  test(
    "suspendContinuation should throw a CompilerRewriteUnsuccessfulException if it is not converted") {
    def foo()(using Suspend): Int =
      summon[Suspend].suspendContinuation[Int] { _ => () }

    intercept[Suspend.CompilerRewriteUnsuccessfulException.type](foo())
  }
}
