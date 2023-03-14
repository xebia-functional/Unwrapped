package continuations

import continuations.*

def program = {
  println(ExampleObject.continuations(1))
}

object ExampleObject {
  def continuations(x: Int): Suspend ?=> Int = {
    def method1(x: Int) = x + 1
    val z1 = 1

    summon[Suspend].shift[Int] { continuation =>
      def method2(x: Int) = x + 1
      val z2 = 1

      continuation.resume(method1(x) + method2(x) + z1 + z2 + 1)
    }

    def method3(x: Int) = x + 1
    val z3 = 1

    method3(x) + z1 + z3 + 1
  }
}

