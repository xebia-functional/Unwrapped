package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object StreamsTests extends Properties("Streams tests"):

  property("toList") = forAll { (list: List[Int]) => streamOf(list*).toList == list }

  property("indexed") = forAll { (list: List[Int]) =>
    streamOf(list*).zipWithIndex.toList == list.zipWithIndex
  }

end StreamsTests
