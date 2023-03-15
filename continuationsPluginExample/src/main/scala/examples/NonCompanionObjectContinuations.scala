package examples

import continuations.Suspend

// runs only with scalac
@main def NonCompanionObjectContinuations(): Unit =
  println(ExampleObj.continuations(1, 2))

object ExampleObj:
  private def method1(x: Int) = x + 1
  protected val z1 = 1

  def continuations(x: Int, y: Int)(using s: Suspend): Int =
    def method2(x: Int) = x + 1
    val z2 = 1

    val suspension1 = s.shift[Int] { continuation =>
      def method3(x: Int) = x
      val z3 = 1

      continuation.resume {
        val z4 = 1
        def method4(x: Int) = x

        method1(x) + 1 + z1 + z2 + method2(y) + z3 + method3(x) + z4 + method4(x)
      }
    }

    s.shift[Int] { continuation => continuation.resume(method1(x) + 1) }

    val z5 = suspension1
    def method5(x: Int) = x

    val suspension2 = s.shift[Int] { continuation =>
      continuation.resume(z5 + suspension1 + method5(y))
    }

    val z6 = 1
    def method6(x: Int) = x

    1 + suspension1 + suspension2 + z6 + method1(x) + method2(x) + method5(x) + method6(x)
