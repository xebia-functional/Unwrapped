package continuations

def foo(x: Int)(using s: Suspend): Int =
  s.shift[Int] { continuation =>
    def test(x: Int) = x + 1
    continuation.resume(test(1) + 1)
  }
