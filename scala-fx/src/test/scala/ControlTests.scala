package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object ControlTests extends Properties("Control Tests"):

  property("short-circuit from nested control") = forAll { (s: String) =>
    def outer: Control[String] ?=> Int = 1
    def inner: Control[String] ?=> Int =
      s.shift[Int] + outer + 1
    run(inner) == s
  }

  property("happy path") = forAll { (n: Int) =>
    def effect: Control[Nothing] ?=> Int = n
    run(effect) == n
  }

  property("shift short-circuits") = forAll { (s: String) =>
    def effect: Control[String] ?=> Int = s.shift
    run(effect) == s
  }

end ControlTests
