package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object RuntimeTests extends Properties("Runtime Tests"):
  property("Either binding toOption") = forAll { (a: Int, b: Int) =>
    val effect: Int * Bind = Right(a).bind + Right(b).bind
    toOption(effect) == Some(a + b)
  }

  property("Option binding toEither") = forAll { (a: Int, b: Int) =>
    val effect: Int * Bind = Some(a).bind + Some(b).bind
    toEither(effect) == Right(a + b)
  }

  property("Binding two values of different types toEither") = forAll {
    (a: Int, b: Int) =>
      val effect: Int * Bind = Right(a).bind + Some(b).bind
      toEither(effect) == Right(a + b)
  }

  property("Short-circuiting with Either.Left toEither") = forAll {
    (n: Int, s: String) =>
      val effect: Int * Control[String | None.type] =
        Left[String, Int](s).bind + Some(n).bind
      toEither(effect) == Left[String, Int](s)
  }

  property("Short-circuiting with Option.None toOption") = forAll { (n: Int) =>
    val effect: Int * Control[None.type] =
      Right(n).bind + Option.empty[Int].bind
    toOption(effect) == None
  }

end RuntimeTests
