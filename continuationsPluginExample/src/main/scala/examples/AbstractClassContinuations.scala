package examples

import continuations.Suspend

// runs only with scalac
@main def AbstractClassContinuations(): Unit =
  val ec = new ExampleClass1 {}
  val ec2A = new ExampleClass2("AA") {}
  val ec2B = new ExampleClass2Impl("AAA")
  val ec3 = new ExampleClass3Impl("AAA")
  val ec4 = new ExampleClass4 {}
  println(ec.continuations(1, 2))
  println(ec2A.continuations(1, 2))
  println(ec2B.continuations(1, 2))
  println(ec3.continuations(1, 2))
  println(ec4.continuations(1, 2))

  println(new ExampleClass1 {}.continuations(1, 2))
  println(new ExampleClass2("AA") {}.continuations(1, 2))
  println(new ExampleClass2Impl("AAA").continuations(1, 2))
  println(new ExampleClass3Impl("AAA").continuations(1, 2))
  println(new ExampleClass4 {}.continuations(1, 2))

abstract class ExampleClass1:
  import ExampleClass1.p
  import ExampleClass1.method1

  private def method2(x: Int) = x + 1
  protected val qq = 1

  def continuations(x: Int, y: Int)(using s: Suspend): Int =
    val q = p
    val r =
      summon[Suspend].suspendContinuation[Int](
        _.resume(Right(x + q + ExampleClass1.p + method1(y) + method2(y) + qq)))
    r + y + ExampleClass1.p + q + method1(x) + method2(y) + qq

object ExampleClass1:
  private val p = 1
  def method1(x: Int) = x + 1

abstract class ExampleClass2(i: String):
  import ExampleClass2.p
  import ExampleClass2.method1

  private def method2(x: Int) = x + 1
  protected val qq = 1

  def continuations(x: Int, y: Int)(using s: Suspend): Int =
    val q = p
    val r =
      summon[Suspend].suspendContinuation[Int](
        _.resume(Right(x + q + i.length + ExampleClass2.p + method1(y) + method2(y) + qq)))
    r + y + ExampleClass2.p + q + method1(x) + i.length + method2(y) + qq

object ExampleClass2:
  private val p = 1
  def method1(x: Int) = x + 1

class ExampleClass2Impl(i: String) extends ExampleClass2(i)

abstract class ExampleClass3(i: String):
  import ExampleClass3.p

  protected def method2(x: Int) = x + 1
  protected val qq = 1

  def continuations(x: Int, y: Int)(using s: Suspend): Int // =
//    1
//    summon[Suspend].suspendContinuation[Int](_.resume(Right(1)))

object ExampleClass3:
  val p = 1
  def method1(x: Int) = x + 1

class ExampleClass3Impl(i: String) extends ExampleClass3(i) {
  import ExampleClass3.p
  import ExampleClass3.method1

  override def continuations(x: Int, y: Int)(using s: Suspend): Int =
    val q = p
    val r =
      summon[Suspend].suspendContinuation[Int](
        _.resume(Right(x + q + i.length + ExampleClass3.p + method1(y) + method2(y) + qq)))
    r + y + ExampleClass3.p + q + method1(x) + i.length + method2(y) + qq
}

abstract class ExampleClass4:
  private def method1(x: Int) = x + 1
  protected val z1 = 1

  def continuations(x: Int, y: Int)(using s: Suspend): Int =
    def method2(x: Int) = x + 1
    val z2 = 1

    val suspension1 = s.suspendContinuation[Int] { continuation =>
      def method3(x: Int) = x
      val z3 = 1

      continuation.resume {
        val z4 = 1
        def method4(x: Int) = x

        Right(method1(x) + 1 + z1 + z2 + method2(y) + z3 + method3(x) + z4 + method4(x))
      }
    }

    s.suspendContinuation[Int] { continuation => continuation.resume(Right(method1(x) + 1)) }

    val z5 = suspension1
    def method5(x: Int) = x + 1

    val suspension2 = s.suspendContinuation[Int] { continuation =>
      continuation.resume(Right(z5 + suspension1 + method5(y)))
    }

    val z6 = 1
    def method6(x: Int) = x + 1

    1 + suspension1 + suspension2 + z6 + method1(x) + method2(x) + method5(x) + method6(x)
