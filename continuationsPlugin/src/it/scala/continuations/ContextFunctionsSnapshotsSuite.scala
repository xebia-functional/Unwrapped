package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.SnapshotSuite

class ContextFunctionsSnapshotsSuite extends SnapshotSuite, CompilerFixtures:

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-1") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> Int = 1
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-2") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> (String, Int) => Int = (w, x) => x + w.length
          | println(foo("AA", 1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-3") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> (Int, String) => Int => Int = (x, w) => y => y + x + w.length
          | println(foo(1, "AA")(3))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-4") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> (Int, String) => Int ?=> Int = (x, w) => x + w.length + summon[Int]
          | given Int = 3
          | println(foo(1, "AA"))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-5") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int => Suspend ?=> Int = x => x
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-6") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int => Suspend ?=> Int => Int = x => y => x + y
          | println(foo(1)(2))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-7") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int ?=> Suspend ?=> Int = summon[Int]
          | given Int = 3
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-8") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: (Int, Suspend) ?=> Int => Int = x => x + summon[Int]
          | given Int = 3
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-9") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int => Int => Suspend ?=> Int => String => Int => Int = x =>
          |    y => z => w => q => x + y + z + w.length + q
          | println(foo(1)(2)(3)("AAAA")(5))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-10") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int => Int => Suspend ?=> (String, Boolean) => Int => Int = x =>
          |    z => (w, b) => y => x + y + z + w.length + b.toString.length
          | println(foo(1)(2)("AAA", false)(4))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-11") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: (Suspend, Int) ?=> Int => Int => Int = x => y => x + summon[Int] + y
          | given Int = 3
          | println(foo(1)(2))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-12") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int => (String, Boolean) => Int => Suspend ?=> Int => Int = x =>
          |    (w, b) => z => y => x + y + z + w.length + b.toString.length
          | println(foo(1)("AA", true)(3)(4))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-13") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo(x: Int, y: Int)(z: Int, k: Int): Suspend ?=> Int ?=> (Int, Int) => Int =
          |    (m, n) => x + y + z + k + m + n + summon[Int]
          | given Int = 3
          | println(foo(1, 2)(3, 4)(5, 6))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-14") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: List[Int] => Suspend ?=> Int = l => l.size
          | println(foo(List(1, 2)))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-15") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> [A] => List[A] => Int = [A] => (l: List[A]) => l.size
          | println(foo(List("AA", "AA")))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-16") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: List[String] ?=> Suspend ?=> Int = summon[List[String]].size
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-17") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Int ?=> Suspend ?=> String ?=> List[String] ?=> Int =
          |    summon[Int] + summon[String].length + summon[List[String]].size
          | given Int = 3
          | given String = "AA"
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-18") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> Int ?=> String ?=> List[String] ?=> Int =
          |    summon[Int] + summon[String].length + summon[List[String]].size
          | given Int = 3
          | given String = "AA"
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-19".clear) {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo(using Suspend, Int): String ?=> List[String] ?=> Int =
          |    summon[Int] + summon[String].length + summon[List[String]].size
          | given Int = 3
          | given String = "AA"
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-20") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: (Int, Suspend) ?=> String ?=> Int => Int =
          |    x => x + summon[Int] + summon[String].length
          | given Int = 3
          | given String = "AA"
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-21") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> Int = {
          |    println("Hello")
          |    val x = 3
          |    1 + x
          | }
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-22") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> Int => Int ?=> Int = y => {
          |    val x = 3
          |    println("Hello")
          |    1 + x + y + summon[Int]
          | }
          | given Int = 3
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function def-23") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | def foo: Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int = {
          |    [A] =>
          |      (x: List[A]) =>
          |        [B] =>
          |          (y: List[B]) =>
          |            (q: List[A]) =>
          |              (p: List[A]) =>
          |                val z = 1
          |                x.size + y.size + q.size + p.size + z
          | }
          | println(foo(List(1))(List("A", "B"))(List(1, 1, 1))(List(1, 1, 1, 1)))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-1") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> Int = 1
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-2") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> (String, Int) => Int = (w, x) => x + w.length
          | println(foo("AA", 1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-3") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> (Int, String) => Int => Int = (x, w) => y => y + x + w.length
          | println(foo(1, "AA")(3))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-4") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> (Int, String) => Int ?=> Int = (x, w) => x + w.length + summon[Int]
          | given Int = 3
          | println(foo(1, "AA"))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-5") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int => Suspend ?=> Int = x => x
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-6") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int => Suspend ?=> Int => Int = x => y => x + y
          | println(foo(1)(2))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-7") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int ?=> Suspend ?=> Int = summon[Int]
          | given Int = 3
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-8") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: (Int, Suspend) ?=> Int => Int = x => x + summon[Int]
          | given Int = 3
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-9") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int => Int => Suspend ?=> Int => String => Int => Int = x =>
          |    y => z => w => q => x + y + z + w.length + q
          | println(foo(1)(2)(3)("AAAA")(5))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-10") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int => Int => Suspend ?=> (String, Boolean) => Int => Int = x =>
          |    z => (w, b) => y => x + y + z + w.length + b.toString.length
          | println(foo(1)(2)("AAA", false)(4))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-11") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: (Suspend, Int) ?=> Int => Int => Int = x => y => x + summon[Int] + y
          | given Int = 3
          | println(foo(1)(2))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-12") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int => (String, Boolean) => Int => Suspend ?=> Int => Int = x =>
          |    (w, b) => z => y => x + y + z + w.length + b.toString.length
          | println(foo(1)("AA", true)(3)(4))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-13") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: List[Int] => Suspend ?=> Int = l => l.size
          | println(foo(List(1, 2)))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-14") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> [A] => List[A] => Int = [A] => (l: List[A]) => l.size
          | println(foo(List("AA", "AA")))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-15") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: List[String] ?=> Suspend ?=> Int = summon[List[String]].size
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-16") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Int ?=> Suspend ?=> String ?=> List[String] ?=> Int =
          |    summon[Int] + summon[String].length + summon[List[String]].size
          | given Int = 3
          | given String = "AA"
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-17") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> Int ?=> String ?=> List[String] ?=> Int =
          |    summon[Int] + summon[String].length + summon[List[String]].size
          | given Int = 3
          | given String = "AA"
          | given List[String] = List("AA", "B")
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-18") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: (Int, Suspend) ?=> String ?=> Int => Int =
          |    x => x + summon[Int] + summon[String].length
          | given Int = 3
          | given String = "AA"
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-19") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> Int = {
          |    println("Hello")
          |    val x = 3
          |    1 + x
          | }
          | println(foo)
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-20") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> Int => Int ?=> Int = y => {
          |    val x = 3
          |    println("Hello")
          |    1 + x + y + summon[Int]
          | }
          | given Int = 3
          | println(foo(1))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }

  compilerContextWithContinuationsPlugin.snapshotTest("context function val-21") {
    case given Context =>
      val source =
        """
          |import continuations.*
          |
          |def program = {
          | val foo: Suspend ?=> [A] => List[A] => [B] => List[B] => List[A] => List[A] => Int = {
          |    [A] =>
          |      (x: List[A]) =>
          |        [B] =>
          |          (y: List[B]) =>
          |            (q: List[A]) =>
          |              (p: List[A]) =>
          |                val z = 1
          |                x.size + y.size + q.size + p.size + z
          | }
          | println(foo(List(1))(List("A", "B"))(List(1, 1, 1))(List(1, 1, 1, 1)))
          |}
          |""".stripMargin

      continuationsCompilerSnapshot(source)
  }
