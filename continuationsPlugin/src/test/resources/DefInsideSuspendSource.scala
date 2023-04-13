package continuations

def foo(x: Int)(using Suspend): Int =
  shift[Int] { continuation =>
    def test(x: Int) = x + 1
    continuation.resume(test(1) + 1)
  }
