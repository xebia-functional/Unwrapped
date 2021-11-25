package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object ControlTests extends Properties("Control Tests"):

  property("short-circuit from nested control") = forAll {
    (s: String) =>
      def outer: Int * Control[String] = 1
      def inner: Int * Control[String] =
        s.shift[Int] + outer + 1
      run(inner) == s
  }

  property("happy path") = forAll { (n: Int) =>
    def effect: Int * Control[Nothing] = n
    run(effect) == n
  }

  property("shift short-circuits") = forAll { (s: String) =>
    def effect: Int * Control[String] = s.shift
    run(effect) == s
  }

end ControlTests
