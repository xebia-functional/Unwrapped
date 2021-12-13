package fx

import cats.syntax.option._
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object RuntimeTests extends Properties("Runtime Tests"):
  property("Either binding toOption") = forAll { (a: Int, b: Int) =>
    val effect: Int * Bind * Control[Nothing] = Right(a).bind + Right(b).bind
    run(effect.toOption) == Some(a + b)
  }

  property("Option binding toEither") = forAll { (a: Int, b: Int) =>
    val effect: Int * Bind * Control[None.type] =
      Some(a).bind + Some(b).bind
    run(effect.toEither) == Right(a + b)
  }

  property("Binding two values of different types toEither") = forAll {
    (a: Int, b: Int) =>
      val effect: Int * Bind * Control[None.type] = Right(a).bind + Some(b).bind
      run(effect.toEither) == Right(a + b)
  }
end RuntimeTests
