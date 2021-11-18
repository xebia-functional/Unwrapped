package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object ContTests extends Properties("Cont laws") :

  // property("Can short-circuit immediately from nested blocks") =
  //   forAll { (s: String) =>
  //     cont[String, Int] {
  //       cont[Int, Int] {
  //         shift(s)
  //       }.fold(identity, identity)
  //     }.fold(identity, identity) == s
  //   }

  // property("happy path") =
  //   forAll { (n: Int) =>
  //     cont[String, Int] {
  //       n
  //     }.fold(identity, identity) == n
  //   }

  // property("short-circuit") =
  //   forAll { (s: String) =>
  //     cont[String, Int] {
  //       shift(s)
  //     }.fold(identity, identity) == s
  //   }
  

end ContTests