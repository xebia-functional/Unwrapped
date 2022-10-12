package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object RuntimeTests extends Properties("Runtime Tests"):
  property("Either binding toOption") = forAll { (a: Int, b: Int) =>
    val effect: Int = Right(a).bind + Right(b).bind
    toOption(effect).contains(a + b)
  }

  property("Option binding toEither") = forAll { (a: Int, b: Int) =>
    val effect: Int = Some(a).bind + Some(b).bind
    toEither(effect) == Right(a + b)
  }

  property("Binding two values of different types toEither") = forAll { (a: Int, b: Int) =>
    val effect: Int = Right(a).bind + Some(b).bind
    toEither(effect) == Right(a + b)
  }

  property("Short-circuiting with Either.Left toEither") = forAll { (n: Int, s: String) =>
    val effect: Raise[String | None.type] ?=> Int =
      Left[String, Int](s).bind + Some(n).bind
    toEither(effect) == Left[String, Int](s)
  }

  property("Short-circuiting with Option.None toOption") = forAll { (n: Int) =>
    val effect: Raise[None.type] ?=> Int =
      Right(n).bind + Option.empty[Int].bind
    toOption(effect).isEmpty
  }

end RuntimeTests
