package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object ControlTests extends Properties("Control Tests"):

  property("short-circuit from nested control") = forAll { (s: String) =>
    def outer: Raise[String] ?=> Int = 1
    def inner: Raise[String] ?=> Int =
      s.raise[Int] + outer + 1
    run(inner) == s
  }

  property("happy path") = forAll { (n: Int) =>
    def effect: Raise[Nothing] ?=> Int = n
    run(effect) == n
  }

  property("raise short-circuits") = forAll { (s: String) =>
    def effect: Raise[String] ?=> Int = s.raise
    run(effect) == s
  }

end ControlTests
