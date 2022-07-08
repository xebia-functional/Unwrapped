# Pre-SIP: Suspended functions and continuations in Scala 3.

This Pre-SIP post proposes continuations as a new language feature in Scala 3.

It has been prepared by the Scala 3 team at 47 Degrees.
This doc is intended to be used as a way to gather community feedback and foster discussion.

## Motivation

Our observation in the industry and among our peers is that most programming in Scala today that involves async or I/O-based programs targets a monadic indirect boxed style.
Most programs involve some form of async effects, and in that case, they largely depend on data types such as `Future`, or lazy `IO` types found in many community libraries.
These data types express dependent, parallel, asynchronous, or potentially erroneous computations as lazily evaluated values or thread-shifted eager computations.
They do so to maintain efficient parallelization or concurrent execution, error-handling properties, non-determinism, and simplified structured concurrency.
This indirect style allows the programmer to treat side-effects as if they were any other value.

Library-level combinators such as `map`, `flatMap`, and `raiseError` allow the composition of single monads to compose relatively freely and easily.
However, combining multiple side-effects often involves increasingly confusing methods and datatypes to separate program expression from execution and treat the program as a value.
This style requires knowledge of and strict adherence to complex algebraic laws. These laws take time and effort to absorb and understand.

In scala, where the execution of side-effects is not yet tracked at the language level, it takes great discipline to maintain reasonable guarantees of safety, composition, and correctness in constructing data types concordance with these laws. The data structures required to maintain adherence to these laws in side-effecting programs do not generally compose. Complex attempts to unify the simplicity of function composition with monadic extensible effect/transformer systems increase the distance between programmer intent and program expression.

Concepts such as simple tail recursion, loops, and try/catch must be sacrificed to maintain safety, program throughput and reasonableness guarantees when adhering to a monadic style.

We would like to write scala programs in a direct style while maintaining the safety of the indirect monadic style. We would like to write these programs with a unified syntax, regardless of these programs being async or sync. We have experienced this programming style in Kotlin for the last few years
with [`suspend`](https://kotlinlang.org/docs/composing-suspending-functions.html) functions. We have found that these programs are easier to write and teach and generally perform better than those written in indirect style.

We think most of the features we need are already on Scala 3, but we lack a way to perform non-blocking async/sync IO in direct style.

## Example

Given a model mixing a set of unrelated monadic datatypes such as `Option`, `Either`, and `Future`, we would like to access the country code given a `Future[Person]`

```scala
import scala.concurrent.Future

object NotFound

case class Country(code: Option[String])

case class Address(country: Option[Country])

case class Person(name: String, address: Either[NotFound.type, Address])
```

Instead of the encodings we see today based on `map` and `flatMap` (or equivalent for comprehensions) like the one below.

```scala
import scala.concurrent.Future

def getCountryCodeIndirect(futurePerson: Future[Person]): Future[Option[String]] =
  futurePerson.map { person =>
    person.address match
      case Right(address) =>
        address.country.flatMap(_.code)
      case Left(_) =>
        None
  }
```

We would like to be able to express the same program in a direct style where instead of
returning a `Future[Option[String]]` we return just `String`.

```scala
import scala.concurrent.Future

suspend def getCountryCodeDirect(futurePerson: Future[Person])
     (using Structured, Control[NotFound.type | None.type]): String =
  val person = futurePerson.bind
  val address = person.address.bind
  val country = address.country.bind
  country.code.bind
```

The program above is impossible to implement in a direct style today without blocking because a call to `futurePerson.bind` would have to use `Await` or similar.

The program in the example above uses the `Control` type to represent the possibility of failure.

Invoking `getCountryCodeDirect` returns a `String` but until `Control` is resolved it may also contain `NotFound` or `None`.

We can take it further and simplify if `bind` is defined as `apply`:

```scala
suspend def getCountryDirect2(futurePerson: Future[Person])
     (using Structured, Control[NotFound.type | None.type]): String =
  futurePerson().address().country().code()
```

## Status Quo & Alternatives

In Scala, interleaving monadic data types in a direct style (including `Future` and lazy `IO`) is impossible.
Despite context functions and the upcoming capture checking system, Scala lacks an underlying system such as Kotlin continuations or Java LOOM, where functions can suspend and resume computations.

Other projects such as [dotty-cps-async](https://github.com/rssh/dotty-cps-async) or [Monadless](https://github.com/monadless/monadless)
provide similar syntactic sugar for monads and do a great job about it.
We have enjoyed using these libraries, but after trying native language support for these features in Kotlin, we decided to propose a deeper integration that works over function declarations and not just expressions.

### Other communities and languages

Other communities and languages concerned about ergonomics and performance, like Kotlin and Java, are bringing the notion of native support for scoped [continuations](https://kotlinlang.org/docs/composing-suspending-functions.html#async-style-functions) and structured concurrency.
These features allow for sync and async programming without boxed return types and indirect monadic style.

These languages implement such techniques in different ways. In the case of Kotlin, [the compiler performs CPS transformations](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md#state-machines) for `suspend` functions, eliminating the need for callbacks and simplifying function return types.
This simple native compiler-supported keyword allows other ecosystem libraries such as [Spring](https://spring.io/blog/2019/04/12/going-reactive-with-spring-coroutines-and-kotlin-flow#webflux-with-coroutines-api-in-kotlin), [Android](https://developer.android.com/kotlin/coroutines/coroutines-adv), and many other libraries and projects in the Kotlin ecosystem integrate with suspending functions natively.

JDK 19, the Java 19 hotspot runtime, and [Project Loom](https://openjdk.org/projects/loom/) include support for virtual threads and structured concurrency built on top of [continuations](https://github.com/openjdk/loom/blob/132bc6aacdf5dfa4897e44772bc7b2052fc2f2d2/src/hotspot/share/runtime/continuation.cpp)

## Proposal

We want to propose a native system for continuations in Scala.

Two possible implementations are included in this Pre-SIP Post:

1. The addition of a new keyword, `suspend`.
    ```scala
    suspend def countryCode: String
    ```

2. The use of compiler-desugared `Suspend` context functions or given/using evidence.
    ```scala
    def countryCode: Suspend ?=> String
    ```

Our intuition is that this could be part of the experimental [Capture Checking](https://www-dev.scala-lang.org/scala3/reference/experimental/cc.html) and related to the experimental [CanThrow](https://docs.scala-lang.org/scala3/reference/experimental/canthrow.html) capabilities, where the compiler performs special desugaring in the function body.

### Potential implementation

If the compiler followed a model similar to Kotlin, suspended function and lambdas get to codegen with an additional parameter.

`suspend def countryCode: String` is desugared to a function that looks like in bytecode like `def countryCode(continuation: Continuation[?]): ?`.

The body of the suspended function desugars to a [state machine](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md#state-machines) where each state is labeled and associated with suspension points.
In the function `countryCode`, calls to `bind` are calls to suspended functions and are considered suspension points.
When a program reaches a suspension point, the underlying continuation may have suspended, performed some work, and resumed back to the original control flow when ready.
The continuation can perform this background work without blocking the caller.

### Use cases

#### Removing callbacks

In the example below, we can define `bind`, a function that returns `A` from a `Future[A]` without blocking.

```scala
extension [A](f: Future[A])(using ExecutionContext)
    suspend def bind: A =
      continuation[A] { cont: Continuation[A] =>
        f.onComplete {
          //naive impl does not look into cancellation wiring.
          _.fold(ex => cont.resumeWithException(ex), cont.resume)
        }
      }
      
```

We use `continuation` to create a continuation that suspends the current program and resumes it when the future completes.
`continuation` is only available when the user is inside the scope of a `suspend` function.
Continuations can be resumed with the expected value or an exception.

```scala
trait Continuation[A]:
  def resume(a: A): Unit
  def resumeWithException(e: Throwable): Unit

// compiler generated platform dependent implementation for Continuation
suspend def continuation[A](c: Continuation[A] => Unit): A =
  ???
``` 

#### Structured concurrency

Once we have continuations available we can build structured blocks. These blocks guarantee asynchronous tasks spawned inside complete
before the block is exited either with a successful result or an exception.

The following example uses project LOOM dependencies with Scala 3 and wraps their structured concurrency implementation.
If we don't depend on LOOM this example would be blocking each time the fibers are joined.
If continuations where available, we could use them to avoid blocking and have other impls outside of LOOM and the JVM.
Compiling and running this code requires VM args `--add-modules jdk.incubator.concurrent` and a [build of JDK 19 with LOOM](https://jdk.java.net/loom/).

```scala
import jdk.incubator.concurrent.StructuredTaskScope
import scala.annotation.implicitNotFound
import java.util.concurrent.*

@implicitNotFound(
  "Structured concurrency requires capability:\n% Structured"
)
opaque type Structured = StructuredTaskScope[Any]

extension (s: Structured)
  private[fx] def forked[A](callable: Callable[A]): Future[A] =
    s.fork(callable)

inline def structured[B](f: Structured ?=> B): B =
  val scope = new StructuredTaskScope[Any]()
  given Structured = scope
  try f
  finally
    scope.join
    scope.close()

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  new Callable[A] { def call(): A = f() }

opaque type Fiber[A] = Future[A]

extension [A](fiber: Fiber[A])
  def join: A = fiber.get // this is non-blocking in LOOM
  def cancel(mayInterrupt: Boolean = true): Boolean =
     fiber.cancel(mayInterrupt)

def fork[B](f: () => B)(using structured: Structured): Fiber[B] =
   structured.forked(callableOf(f))
```

Structured blocks are resources that when they get closed, they join all fibers that were created within the block.

We can implement different policies with structured concurrency, such as:
   - Shutdown on failure
   - Shutdown on success
   - Control the number of fibers to join or parallelism level.

In the program below all fibers are joined before the `structured` block exits.

```scala
    val x: Control[Int] ?=> Structured ?=> String =
      val fa = fork[String](() => "Hello")
      val fb = fork[String](() => 0.shift)
      fa.join + fb.join

    val value: String | Int = run(structured(x)) 
```


#### Functional programming based on continuation folding

Many functional patterns such as safe error handling can be derived from continuations.

`Control` implements the classic `Control/shift` from continuations literature to demonstrate an application of continuations and exceptions for safe functional error handling.

We can think of a continuation as a program producing `A` or a `Throwable`, but when it's using `Control`, it may be interrupted at any point with a value of `R`.
`Control` provides `shift` the operation that allows interruption analogous to the imperative `throw` but it's not restricted to `Throwable`.

```scala
trait Control[-R]: //can potentially be implemented in terms of `canThrow`
  extension (r: R) 
    suspend def shift[A]: A // can throw or shift to R when otherwise expected A
```

All programs requiring `Control` are foldable and they interop with `try/catch`

```scala
import java.util.UUID
import java.util.concurrent.ExecutionException
import scala.annotation.tailrec
import scala.util.control.ControlThrowable

object Continuation:
  inline suspend def fold[R, A, B](
      inline program: suspend Control[R] ?=> A
  )(inline recover: suspend R => B, inline transform: suspend A => B): B = {
    var result: Any | Null = null
    implicit val control = new Control[R] {
      val token: String = UUID.randomUUID.toString

      extension (r: R)
        def shift[A]: A =
          throw ControlToken(token, r, recover.asInstanceOf[Any => Any])
    }
    try {
      result = transform(program(using control))
    } catch {
      case e: Throwable =>
        result = handleControl(control, e)
    }
    result.asInstanceOf[B]
  }

  @tailrec def handleControl(control: Control[_], e: Throwable): Any =
    e match
      case e: ExecutionException =>
        handleControl(control, e.getCause)
      case e @ ControlToken(token, shifted, recover) =>
        if (control.token == token)
          recover(shifted)
        else
          throw e
      case _ => throw e

  private case class ControlToken(
      token: String,
      shifted: Any,
      recover: Any => Any
  ) extends ControlThrowable
```

In the implementation above, `program`, `recover` and `transform` are all suspended functions.
We can `try`/`catch` over them because they are suspension points, and they guarantee control flow will return to the caller either with a result or an exception. 
The work performed may go async, get scheduled, or sleep, all in a non-blocking way.

`run` and other similar operators that fold the program look like:

```scala
extension [R, A](c: Control[R] ?=> A)

    def toEither: Either[R, A] =
      fold(c)(Left(_), Right(_))

    def toOption: Option[A] =
      fold(c)(_ => None, Some(_))

    def run: (R | A) = fold(c)(identity, identity)
```

For a full impl with more operators and abstractions,
see [EffectScope](https://github.com/arrow-kt/arrow/blob/main/arrow-libs/core/arrow-core/src/commonMain/kotlin/arrow/core/continuations/EffectScope.kt) the equivalent to `Control` and
[`fold`](https://github.com/arrow-kt/arrow/blob/main/arrow-libs/core/arrow-core/src/commonMain/kotlin/arrow/core/continuations/Effect.kt#L760) impl in Arrow.

Once we have the ability to `Control` and `shift` we can implement monad bind for types like `Either` and `Option`. 
Here monad bind has the shape `F[A] => A`. Once we have a function like `bind`, we can extract `A`
without needing to `map` over `F`. If we encounter a failure case at any point, we will not get `A`, and our program
short-circuits up to the nearest `Control` in the same way exceptions work.

```scala
extension [R, A](fa: Either[R, A]) 
  suspend def bind(using Control[R]): A = 
    fa.fold(_.shift, identity) //shifts on Left

extension [A](fa: Option[A])
  suspend def bind(using Control[None.type]): A = 
    fa.fold(None.shift)(identity) //shifts on None
```

We can safely compose unrelated types with `bind` in the same scope.
`shift` allows us to escape the continuation in case we encounter a `Left` or `None`.

With the implementations for `bind` we can express now `countryCode` in a direct, non-blocking style.

```scala
def getCountryCodeDirect(futurePerson: Future[Person])
    (using Structured, Control[NotFound.type | None.type]): String =
  val person = futurePerson.bind //throws if fails to complete (we don't want to control this)
  val address = person.address.bind //shifts on Left
  val country = address.country.bind //shifts on None
  country.code.bind //shifts on None
```

Monadic values compose in the same scope delegating their computation to the underlying continuation.
There is no need for wrapped return types, monad transformers, or stacked types to model a sequential computation composed of unrelated monadic types.

We don't propose `bind` or `Control` as part of this proposal, just intrinsics for continuations such as the function `continuation`.

Finally, we have used `using` clauses to model functions with effects or context functions to model programs as values with
`given` effect requirements.

#### Is the answer Traverse?

In this model `traverse` can be simply defined as `map` + `bind`.

```scala
@main def program2 =
  val test: Structured ?=> Control[String] ?=> List[Int] =
    List(Right(1), Right(2), Left("oops")).map(x => x.bind)
  println(run(structured(test))) // oops

@main def program3 =
  val test: Structured ?=> Control[String] ?=> List[Int] =
    List(Right(1), Right(2), Right(3)).map(x => x.bind + 1)
  println(run(structured(test))) // List(2, 3, 4)
```

#### Non-blocking sleep

Since continuations don't block, we can schedule their completion and resume them when needed.

```scala
private val executor = Executors.newSingleThreadScheduledExecutor((runnable: Runnable) => {
  val thread = Thread(runnable, "scheduler")
  thread.setDaemon(true)
  thread
})

suspend def sleepMillis(time: Long): Unit = continuation { c =>
  val task = new Runnable:
    override def run(): Unit = c.resume(())
  executor.schedule(task, time, TimeUnit.MILLISECONDS)
}
```

[kotlin example](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md#non-blocking-sleep)

#### Generators

Operators such as `yield` are helpful in generators that suit stream processing.
In this model, only when the caller requests an element `yield` computes it and offers it back.

```scala
val fibonacci: LazyList[Int] = lazyList { //suspend lambda
    yield(1) // first Fibonacci number (suspension point)
    var cur = 1
    var next = 1
    while (true) do
      yield(next) // next Fibonacci number (suspension point)
      val tmp = cur + next
      cur = next
      next = tmp
}
```

[kotlin example](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md#generators)

## Additional information

The text for this pre-sip and the code are available in this [gist](https://gist.github.com/raulraja/e75ba94e6d21f7ec1016d2cb62e03803)

## Next steps

We believe that introducing continuations in Scala 3 coupled or not to the capture checking system or context function results in the following improvements:

- Simplifies program description, eliminating wrapped return types for most use cases.
- Helps inference and compile times due to reducing the usage of complex types.
- Cooperates with the language control structures and produces smaller and faster programs that desugar suspension points efficiently in the stack.
- Eases the learning curve to program async / effects-based applications and libraries in Scala.
- Reduces indirection and allocations that arise through higher-order functions used extensively in `map`, `flatMap`, and others.
- Can interop with other libraries and frameworks that offer custom fiber scheduling and cancellation strategies.

Looking forward to your thoughts and feedback, thank you for reading this far! :pray:


