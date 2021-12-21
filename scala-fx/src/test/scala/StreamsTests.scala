package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import org.scalacheck.Test.Parameters

object StreamsTests extends Properties("Streams tests"):

  override def overrideParameters(p: Parameters) =
    p.withMinSuccessfulTests(50000)

  property("send/receive") = forAll { (n: Int) =>
    structured(streamed { send(n) }.toList) == List(n)
  }

  // property("send/receive - n elements") = forAll { (list: List[Int]) =>
  //   streamed { list.foreach(send) }.toList == list
  // }

  // property("streamOf(list*)") = forAll { (list: List[Int]) => streamOf(list*).toList == list }

  // property("zipWithIndex") = forAll { (list: List[Int]) =>
  //   streamOf(list*).zipWithIndex.toList == list.zipWithIndex
  // }

  property("map") = forAll { (n: Int) =>
    val streamResult = structured(streamOf(n).map(_ + 1).toList)
    val listResult = List(n + 1)
    if (streamResult != listResult) {
      println(s"not equals: $streamResult $listResult")
    }
    streamResult == listResult
  }

// property("flatMap") = forAll { (n: Int) =>
//   streamOf(n).flatMap(n => streamOf(n, n + 1)).toList == List(n, n + 1)
// }

// property("comprehensions") = forAll { (n: Int) =>
//   val r = for {
//     a <- streamOf(n)
//     b <- streamOf(n)
//   } yield a + b
//   r.toList == List(n + n)
// }

end StreamsTests
