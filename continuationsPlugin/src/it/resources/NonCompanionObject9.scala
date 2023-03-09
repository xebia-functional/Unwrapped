package continuations

import continuations.*

def program = {
  println(ExampleObject.continuations(1))
}

object ExampleObject {
  private def method1(x: Int) = x + 1
  private val z1 = 1

  protected def method2(x: Int) = x + 1
  protected val z2 = 1

  def method3(x: Int) = x + 1
  val z3 = 1

  def continuations(x: Int): Suspend ?=> Int = {
    val result1 = summon[Suspend].suspendContinuation[Int] { continuation =>
      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
    }

    summon[Suspend].suspendContinuation[Int] { continuation =>
      continuation.resume(Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1))
    }

    val result2 = summon[Suspend].suspendContinuation[Int] { continuation =>
      continuation.resume(
        Right(method1(x) + method2(x) + method3(x) + z1 + z2 + z3 + 1 + result1))
    }

    def method4(x: Int) = x + 1
    val z4 = 1

    1 + method1(x) + method2(x) + method3(x) + method4(
      x) + z1 + z2 + z3 + z4 + result1 + result2
  }
}
