package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object ContTests extends Properties("Cont laws"):

  property("Can short-circuit immediately from nested blocks") = forAll {
    (s: String) =>
      def outer: Int * Control[String] = 1
      def inner: Int * Control[String] =
        s.shift[Int] + outer + 1
      run(inner) == s
  }

  property("happy path") = forAll { (n: Int) =>
    def effect: Int * Control[String] = n
    run(effect) == n
  }

  property("short-circuit") = forAll { (s: String) =>
    def effect: Int * Control[String] = s.shift
    run(effect) == s
  }

end ContTests
