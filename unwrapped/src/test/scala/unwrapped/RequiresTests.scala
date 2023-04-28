package unwrapped

import org.scalacheck.Prop._
import org.scalacheck.Properties

import scala.compiletime.testing.typeCheckErrors
import scala.compiletime.testing.typeChecks

object RequiresTests extends Properties("requires compile-time assertions"):

  property("requires detects false assertions at compile time") = forAll {
    (assertion: Boolean) =>
      assertion ==> {
        val x = typeChecks("""
import unwrapped.*

val x: Boolean = requires(true, "Expected true, was false.", true)""")
        x
      }
      (!assertion) ==> {
        val x = typeCheckErrors("""
import unwrapped.*

val x: Boolean = requires(false, "Some Error Message.", false)""")
        x.head.message == "Some Error Message."
      }
  }
