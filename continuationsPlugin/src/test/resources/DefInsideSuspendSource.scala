package continuations

def foo(x: Int)(using s: Suspend): Int =
  s.suspendContinuation[Int] { continuation =>
    def test(x: Int) = x + 1
    continuation.resume(test(1) + 1)
  }
