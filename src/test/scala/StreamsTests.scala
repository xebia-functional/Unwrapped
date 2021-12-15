package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object StreamsTests extends Properties("Streams tests"):

  property("send/receive") = forAll { (n: Int) => streamed { send(n) }.toList == List(n) }

  property("toList") = forAll { (list: List[Int]) => streamOf(list*).toList == list }

  property("indexed") = forAll { (list: List[Int]) =>
    streamOf(list*).zipWithIndex.toList == list.zipWithIndex
  }

  property("map") = forAll { (n: Int) => streamOf(n).map(_ + 1).toList == List(n + 1) }

  property("flatMap") = forAll { (n: Int) =>
    streamOf(n).flatMap(n => streamOf(n + 1)).toList == List(n + 1)
  }

end StreamsTests
