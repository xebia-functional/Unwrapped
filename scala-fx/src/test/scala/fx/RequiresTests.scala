package fx

import org.scalacheck.Prop._
import org.scalacheck.Properties

import scala.compiletime.testing.typeCheckErrors
import scala.compiletime.testing.typeChecks

object RequiresTests extends Properties("requires compile-time assertions"):

  property("requires detects false assertions at compile time") = forAll {
    (assertion: Boolean) =>
      assertion ==> {
        val x = typeChecks("""
import fx.*

requires(true, "Expected true, was false.")""")
        x
      }
      (!assertion) ==> {
        val x = typeCheckErrors("""
import fx.*

requires(false, "Some Error Message.")""")
        x.head.message == "Some Error Message."
      }
  }
