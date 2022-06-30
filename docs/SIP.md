# Pre-SIP: Suspended functions, continuations, and structured concurrency in Scala 3.

## Motivation

Our observation in the industry and among our peers is that most programming in Scala today that involves async or effect-based programs targets a monadic indirect style, largely depending on data types such as `Future`, or lazy `IO`.
These data types express dependent, parallel, asynchronous or potentially erroneous computations as lazily evaluated values or thread-shifted eager computations to maintain efficient parallelization or concurrent execution; error-handling properties, non-determinism, and/or simplified structured concurrency.
This allows the programmer to treat side-effects as if they were any other value. Library-level combinators such as `map`, `flatMap`, and `raiseError` allow the composition of single monads to compose relatively freely and easily. However, combining multiple side-effects often involves increasingly confusing methods and datatypes to maintain the separation of program expression and execution necessary to maintain the abstraction of treating the program as a value.
This style requires knowledge of and strict adherence to complex algebraic laws. These laws take time and effort to absorb and understand. In scala, where the execution of side-effects is not yet tracked at the language-level, it takes great discipline to maintain reasonable guarantees of safety, composition, correctness in the construction of data types in concordance with these laws. The data structures required to maintain adherence to these laws in side-effecting programs do not generally compose. Complex attempts to unify the simplicity of function composition and monadic extensible effect/transformer systems increases the distance between programmer intent and program expression. Concepts such as simple tail recursion, loops, try/catch and others must be sacrificed to maintain safety, program throughput and reasonableness guarantees when adhering to a monadic style. 


We would like to write scala programs in a direct style while maintaining the safety of the indirect monadic style. We would like to write these programs with a unified syntax,  
regardless of these programs being async or synced in nature. We have experienced this style of programming in Kotlin for the last few years
and when creating equivalent programs in `Future`, `IO` in Scala vs `suspend` in Kotlin we have found that continuation based programs in direct style are easier to write, teach and perform better than those written in indirect style.

We think most of the features we need are already on Scala 3, but we lack a way to perform async/sync IO such as the ones offered by continuation-based systems.

For the given model where we explicitly mix a set of unrelated monadic datatypes such as `Option`, `Either`, and `Future`, we would like to access the country code given an `Option[Person]`

```scala

object NotFound

case class Country(code: Option[String])

case class Address(country: Option[Country])

case class Person(name: String, address: Future[Either[NotFound.type, Address]])
```

Instead of the encodings, we see today based on `map` and `flatMap` or equivalent for comprehensions like the one below.

```scala
def getCountryCode(maybePerson: Option[Person]): Future[Option[String]] =
  maybePerson match
    case Some(person) =>
      person.address.map {
        case Right(address) =>
          address.country.flatMap(_.code)
        case Left(_) =>
          None
      }
    case None => Future.successful(None)
```

we would like to be able to express the same program in a direct style

```scala

def getCountryCodeCont(maybePerson: Option[Person])(using Structured, Control[NotFound.type | None.type]): String =
val person = maybePerson.bind
val addressOrNotFound = person.address.join
val address = addressOrNotFound.bind
val country = address.country.bind
country.code.bind

// or if bind is defined as apply() over Either and Option

def getCountryCodeCont2(maybePerson: Option[Person])(using Structured, Control[NotFound.type | None.type]): String =
  maybePerson().address.join().country().code()

```

## Status Quo

Interleaving monadic datatypes in direct style, including `Future` or lazy IO's, which perform async and sync operations, is impossible in Scala
despite context functions and the upcoming capture checking system. Scala lacks an underlying system such as Kotlin continuations or Java LOOM, where functions can suspend and resume computations.

## Other communities and languages

Other communities and languages concerned about ergonomics and performance, like Kotlin and Java, are bringing the notion of native support for scoped continuations and structured concurrency.
These features allow for sync and async programming without boxed return types and indirect monadic style.

These languages implement such techniques in different ways. In the case of Kotlin, the compiler performs CPS transformations for `suspend` functions, eliminating the need for callbacks and simplifying function return types.
Other ecosystem libraries such as Spring, Compose, and every library and project in the Kotlin ecosystem integrate with suspending functions natively.

In the case of Java and the internals of project LOOM, virtual threads and an impl for non-blocking IO is part of both the new incubator packages in JDK 19 and the internals in the
JVM hotspot where continuations are managed and related to virtual threads.

## Proposal

We want to propose a native system for continuation in Scala.

The examples below demonstrate how this could work either with a
- function modifier: `suspend def helloWorldAsync: String`
- given evidence: `def helloWorldAsync: Suspend ?=> String`

Our original thought is that this could be part of [add links to capture checking] and related
to the [add a link to can throw capability] where the compiler performs special desugar in the function body.

```scala
val helloWorldAsync: Suspended ?=> String =
  val hello = helloFuture.await
  val world = worldFuture.await
  hello + world
```

The `Suspended` ability would unlock `suspendContinuation` or similar operators from which other higher-level ones like
`join` or monad `bind` can be derived.

Alternatively, we can use a function modifier.

```scala
suspend def helloWorldAsync: String =
  val hello = helloFuture.await
  val world = worldFuture.await
  hello + world
```

If the compiler followed a model similar to Kotlin, suspended function and lambdas get to codegen with an additional parameter.

`suspend def helloWorldAsync: String` is desugared to a function that looks like `def helloWorldAsync(continuation: Continuation[?]): Any | Null`.

The body of the suspended function desugars to a state machine where each state is associated with suspension points.
In the function `helloWorldAsync`, both calls to `await` in its body are suspension points where a continuation may have suspended, performed some work, and resumed back to the original control flow.

This desugaring allows for user code to never have to deal with callbacks and allows for integrations with boxed types like `Future,` which can wire themselves to the suspend function by simply completing a call back through their async runners such as `onComplete,` `unsafeRunAsync` or similar.

## Alternative community initiatives

In contrast to other projects such as [link to dotty cps-async] or [link to monad less] where they target `async/await` or you have to
`lift/unlift` monadic structures, we propose a deeper integration in the compiler pipeline that desugars function bodies.

Both `suspend () => A` or context functions with `Suspend ?=> A` can be used to implement Continuation based functional
abstractions that result in simpler programs than those expressed in boxed data types.

## Example library

All is needed for direct syntax that removes callbacks and boxing is a continuation system with some form of compiler
CPS. We don't expect functions like `bind` or `join` to be available in the language, but we do expect `suspend` or similar to be available.

Back to the original paper from [link Filinski paper] and based on previous work in the [link to Arrow Fx framework]

The following examples demonstrate the basics of a small functional library on top of the proposed continuation system
model after the abstractions found in `Arrow` which provides complementary functional abstractions for Kotlin and KotlinX coroutines.

A continuation is a program producing `A` that may be interrupted at any point with a value of `R`.
`Control` provides `shift` the operation that allows interruption and acts as a functional `throw`:

```scala
trait Control[-R]: //can also be implemented in terms of `canThrow`
  extension (r: R) 
    suspend def shift[A]: A // can throw R when otherwise expected R
```

All programs requiring `Control` are foldable.

```scala
suspend fun fold[R, A, B](
      inline program: Control[R] ?=> A
  )(inline recover: R => B, inline transform: A => B): B
```

A helpful operator is monad bind. Here monad bind has the shape `F[A] => A`, yet it has the power to complete the callback in a non-blocking way.

```scala
extension [R, A](fa: Either[R, A]) 
  suspend def bind(using Control[R]): A = 
    fa.fold(_.shift, identity)

extension [A](fa: Option[A])
  suspend def bind(using Control[None.type]): A = 
    fa.fold(None.shift)(identity)
```

We can safely combine our types with `bind` without needing monad transformers.
`shift` allows us to escape the continuation in case we encounter a `Left` or `None`.
Monadic values compose in the same scope delegating their computation to the underlying continuation.
There is no need for wrapped return types, monad transformers or stacked types modeling a sequential computation.

```scala
property("Short-circuiting with Either.Left") = 
  forAll { (n: Int, s: String) =>
    val effect: Control[String | None.type] ?=> Int =
      Left[String, Int](s).bind + Option(n).bind
    run(effect) == s
  }
```

Finally, we have used context functions to model effects that can be executed by folding a continuation.
`run` and other similar operators that fold the continuation are defined as

```scala
extension [R, A](c: Control[R] ?=> A)

    def toEither: Either[R, A] =
      fold(c)(Left(_), Right(_))

    def toOption: Option[A] =
      fold(c)(_ => None, Some(_))

    def run: (R | A) = Continuation.fold(c)(identity, identity)
```

## Improvements

We believe that introducing continuations in Scala 3 coupled or not to the capture checking system or context function results in the following improvements.

- Simplify program description, eliminating wrapped return types for most use cases.
- Help inference and compile times due to reducing the usage of complex types
- Cooperate with the language control structures and produce smaller and faster programs that desugar suspension points efficiently in the stack.
- Ease the learning curve to program async / effects-based applications and libraries in Scala.
- Reduce indirection through higher-order functions used extensively in `map`, `flatMap`, and others.
- ExecutionContext is agnostic. It can interoperate and adhere to different fiber scheduling and cancellation strategies community libraries offer.
    - An analog example of this is KotlinX Coroutines which is not part of the std lib. The std lib exposes continuation intrinsics like `suspendCoroutine`.


Looking for thoughts and feedback, and thank you for reading this far!

