

## Comparison with EPFL-LAMP-Async

The [`async`](https://github.com/lampepfl/async) library 

That library contains a definition of `Suspension` as: 

```scala
/** Contains a delimited contination, which can be invoked with `resume` */
class Suspension[-T, +R]:
  def resume(arg: T): R = ???

def suspend[T, R](body: Suspension[T, R] => R)(using Label[R]): T = ???
```

We can compare this with our `Continuation` and `Suspend` types: 

```scala
trait Continuation[-A]:
  def resume(value: A): Unit
  def raise(error: Throwable): Unit
  // other stuff omitted


sealed trait Suspend:
  def continuation[A](f: Continuation[A] => Unit): A

extension (using Suspend)
  def shift[A](f: Continuation[A] => Unit): A = ???
  // this shift is what we want the compiler to rewrite.
```

It would appear that our `shift` corresponds to their `Suspend`, except that where they use two types `-T` and `+R`, we only used  the first type while the second type is first to `Unit`. Does that mean that our continuations are only procedural? 

Well, part of what happens is that our continuations are resumed by an object who then moves on to do anything else or to be dissolved, so they are truly invoked at the end. This is a bit like saying that they need nothing back.


Now, what is that `Label[R]` itself? 
