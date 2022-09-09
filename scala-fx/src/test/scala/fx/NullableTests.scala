package fx

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalacheck.Prop.propBoolean
import org.scalacheck.Properties

object NullableTests extends Properties("Nullable Tests"):
  given nullConst: Arbitrary[Null] = Arbitrary { Gen.const[Null](null) }
  given nullableInt: Arbitrary[Nullable[Int]] = Arbitrary {
    Gen.oneOf(
      Gen.choose(Int.MinValue, Int.MaxValue).map(Nullable.apply),
      nullConst.arbitrary.map(Nullable.apply[Int]))
  }
  property("Nullable[Int]#value when the value is not null should be the given value") =
    forAll { (a: Int) =>
      val x: Nullable[Int] = Nullable(a)
      run(x.value) == a
    }
  property("Nullable[Int]#value when the value is null should be a null pointer exception") =
    forAll { (a: Null) =>
      val x: Nullable[Int] = Nullable(a)
      val result = run(x.value)
      result match
        case z: Int => false
        case _ => true
    }
  property("Nullable[A]#v is an alias for Nullable[A]#value") = forAll { (a: Int) =>
    val x = Nullable(a)

    a == run(x.v) && run(x.value) == run(x.v)
  }

  property("Nullable[A]#exists is true for non null values") = forAll { (a: Int) =>
    run(Nullable(a).exists)
  }
  property("Nullable[Int]#exists is false for null values") = forAll { (a: Null) =>
    !run(Nullable[Int](a).exists)
  }
  property("Nullable[A]#nonEmpty should be an alias for Nullable[A]#exists") = forAll {
    (a: Int) =>
      val x = Nullable(a)
      run(x.nonEmpty) && run(x.exists) == run(x.nonEmpty)
  }
  property(
    "Nullable[A]#getOrElse(b:A) should return the contained value when the value is non-null") =
    forAll { (a: Nullable[Int], b: Int) => (a.nonEmpty) ==> (a.getOrElse(b) == run(a.v)) }
  property(
    "Nullable#getOrElse(b: A) should return the default value when !Nullable[A]#exists") =
    forAll { (a: Nullable[Int], b: Int) => (!a.exists) ==> (a.getOrElse(b) == b) }
  property(
    "Nullable[A]#orElse(b: Nullable[A]) should return the original nullable when Nullable[A]#nonEmpty") =
    forAll { (a: Nullable[Int], b: Nullable[Int]) => (a.nonEmpty) ==> (a.orElse(b) == a) }
  property(
    "Nullable[A]#orElse(b: Nullable[A]) should return the passed b value when !Nullable[A].exists") =
    forAll { (a: Nullable[Int], b: Nullable[Int]) =>
      (!a.exists && b.nonEmpty) ==> (a.orElse(b) == b)
    }
  property(
    "Nullable[A]#map(f: A => B) should return the application of f to the contained value when Nullable[A].nonEmpty") =
    forAll { (a: Nullable[Int], f: Int => String) =>
      (a.nonEmpty) ==> (run(a.map(f).v) == f(run(a.v).asInstanceOf[Int]))
    }
  property(
    "Nullable[A]#map(f: A => B) should return a non-exstent nullable when !Nullable[A].exists") =
    forAll { (a: Nullable[Int], f: Int => String) => (!a.exists) ==> (!a.map(f).exists) }
  property(
    "Nullable[A].bind(f: A => Nullable[B]) should return the result of applying f to the contained value when Nullable[A]#.nonEmpty") =
    forAll { (a: Nullable[Int]) =>
      val f = (x: Int) => Nullable(x.toString)
      (a.nonEmpty) ==> {
        val x = run(a.value)
        a.bind(f) == f(run(x.asInstanceOf[Int]))
      }
    }
  property("Nullable#none[A] should return a Nullable[A] that does not exist") = forAll {
    (_: Int) => !Nullable.none[Int].exists
  }
