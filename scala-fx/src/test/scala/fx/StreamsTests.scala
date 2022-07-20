package fx

import org.scalacheck.Properties
import org.scalacheck.Prop._

object StreamsTests extends Properties("Streams tests"):

  property("send/receive") = forAll { (n: Int) => streamOf(n).toList == List(n) }

  property("toList") = forAll { (list: List[Int]) => streamOf(list*).toList == list }

  property("zipWithIndex") = forAll { (list: List[Int]) =>
    streamOf(list*).zipWithIndex.toList == list.zipWithIndex
  }

  property("streamOf(list).grouped.toList.flatten should result in the original list") =
    forAll { (v: List[Int], size: Int) =>
      (size > 0) ==> {
        val list = streamOf(v*).grouped(size).toList.flatten
        list == v
      }
    }

  property("streamOf(list).grouped.toList should result in lists of size size, except for the final item, which contains all remaining items") =
    forAll { (v: List[Long]) =>
      (v.size > 10) ==> {
        val list = streamOf(v*).grouped(10).toList.map(_.size == 10)
        list.headOption.exists(identity) && list.lastOption.nonEmpty
      }

    }

  property("flatten: identity") = forAll { (n: Int) =>
    streamOf(n).map(i => streamOf(i)).flatten.toList == List(n)
  }

  property("flatten") = forAll { (list: List[Int]) =>
    streamOf(list*).map(i => streamOf(i, i + 1)).flatten.toList == list.flatMap(i =>
      List(i, i + 1))
  }

  property("fold") = forAll { (initial: Int, list: List[Int], operation: (Int, Int) => Int) =>
    streamOf(list*).fold(initial, operation).toList == List(list.fold(initial)(operation))
  }

  property("map") = forAll { (n: Int) => streamOf(n).map(_ + 1).toList == List(n + 1) }

  property("flatMap") = forAll { (n: Int) =>
    streamOf(n).flatMap(n => streamOf(n + 1)).toList == List(n + 1)
  }

  property("comprehensions") = forAll { (n: Int) =>
    val r = for {
      a <- streamOf(n)
      b <- streamOf(n)
    } yield a + b
    r.toList == List(n + n)
  }

end StreamsTests
