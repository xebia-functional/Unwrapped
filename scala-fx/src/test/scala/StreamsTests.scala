package fx

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object StreamsTests extends Properties("Streams tests"):

  property("send/receive") = forAll { (n: Int) =>
    streamed { send(n) }.toList == List(n)
  }

  property("send/receive - n elements") = forAll { (list: List[Int]) =>
    streamed { list.foreach(send) }.toList == list
  }

  property("streamOf(list*)") = forAll { (list: List[Int]) => streamOf(list*).toList == list }

  property("zipWithIndex") = forAll { (list: List[Int]) =>
    streamOf(list*).zipWithIndex.toList == list.zipWithIndex
  }

  property("map") = forAll { (n: Int) => streamOf(n).map(_ + 1).toList == List(n + 1) }

  property("flatMap") = forAll { (n: Int) =>
    streamOf(n).flatMap(n => streamOf(n, n + 1)).toList == List(n, n + 1)
  }

  property("comprehensions") = forAll { (n: Int) =>
    val r = for {
      a <- streamOf(n)
      b <- streamOf(n)
    } yield a + b
    r.toList == List(n + n)
  }

end StreamsTests
