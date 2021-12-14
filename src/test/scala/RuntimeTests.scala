package fx

import cats.syntax.option._
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object RuntimeTests extends Properties("Runtime Tests"):
  property("Either binding toOption") = forAll { (a: Int, b: Int) =>
    val effect: Int * Bind = Right(a).bind + Right(b).bind
    effect.toOption == Some(a + b)
  }

  property("Option binding toEither") = forAll { (a: Int, b: Int) =>
    val effect: Int * Bind = Some(a).bind + Some(b).bind
    effect.toEither == Right(a + b)
  }

  property("Binding two values of different types toEither") = forAll {
    (a: Int, b: Int) =>
      val effect: Int * Bind = Right(a).bind + Some(b).bind
      effect.toEither == Right(a + b)
  }
end RuntimeTests
