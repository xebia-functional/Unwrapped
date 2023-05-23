package examples

import continuations.Continuation
import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def GCD =
  def isZero(x: Int)(using s: Suspend): Boolean =
    s.shift { (c: Continuation[Boolean]) =>
      if (x == 0) c.resume(true) else c.resume(false)
    }

  def mod(num: Int, div: Int)(using s: Suspend): Int =
    s.shift { (c: Continuation[Int]) => 
      if (num >= div) c.resume(num % div) else c.resume(div % num)
    }

  def gcd(a: Int, b: Int)(using Suspend): Int =
    if isZero(b) then a else gcd(b, mod(a, b)) + 1

  println(SuspendApp(gcd(75, 90)))
