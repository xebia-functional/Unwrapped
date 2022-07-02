# Pre-SIP: Suspended functions, continuations, and structured concurrency in Scala 3.

This Pre-SIP post proposes continuations as a new language feature in Scala 3.

It has been prepared by the Scala MFT team at 47 Degrees reviewed by [Add your github handle here]().
This doc is intended to be used as way to gather community feedback
and foster discussion.

## Motivation

Our observation in the industry and among our peers is that most programming in Scala today that involves async or effect-based programs targets a monadic indirect style.
Most programs involve some form of async effects and in that case they largely depend on data types such as `Future`, or lazy `IO` like types as found in many community libraries.
These data types express dependent, parallel, asynchronous or potentially erroneous computations as lazily evaluated values or thread-shifted eager computations.
They do so to maintain efficient parallelization or concurrent execution; error-handling properties, non-determinism, and/or simplified structured concurrency.
This allows the programmer to treat side-effects as if they were any other value. 
Library-level combinators such as `map`, `flatMap`, and `raiseError` allow the composition of single monads to compose relatively freely and easily. 
However, combining multiple side-effects often involves increasingly confusing methods and datatypes to separate program expression from execution and treat the program as a value.
This style requires knowledge of and strict adherence to complex algebraic laws. These laws take time and effort to absorb and understand. In scala, where the execution of side-effects is not yet tracked at the language-level, it takes great discipline to maintain reasonable guarantees of safety, composition, correctness in the construction of data types in concordance with these laws. The data structures required to maintain adherence to these laws in side-effecting programs do not generally compose. Complex attempts to unify the simplicity of function composition and monadic extensible effect/transformer systems increases the distance between programmer intent and program expression. Concepts such as simple tail recursion, loops, try/catch and others must be sacrificed to maintain safety, program throughput and reasonableness guarantees when adhering to a monadic style. 

We would like to write scala programs in a direct style while maintaining the safety of the indirect monadic style. We would like to write these programs with a unified syntax,  
regardless of these programs being async or synced in nature. We have experienced this style of programming in Kotlin for the last few years
with [`suspend`](https://kotlinlang.org/docs/composing-suspending-functions.html) functions. We have found that these programs are easier to write, teach and generally perform better than those written in indirect style.

We think most of the features we need are already on Scala 3, but we lack a way to perform async/sync IO such as the ones offered by continuation-based systems.

## Example

Given a model mixing a set of unrelated monadic datatypes such as `Option`, `Either`, and `Future`, we would like to access the country code given an `Option[Person]`

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
def countryCode(maybePerson: Option[Person])(using Control[NotFound.type | None.type]): String =
  val person = maybePerson.bind
  val addressOrNotFound = person.address.join
  val address = addressOrNotFound.bind
  val country = address.country.bind
  country.code.bind
```

Or to show the code above can be further reduced if `bind` is defined as `Either.apply` and `Option.apply`:

```scala
def countryCode(maybePerson: Option[Person])(using Control[NotFound.type | None.type]): String =
  maybePerson().address.join().country().code()
```

## Status Quo

Interleaving monadic data types that are not also `Comonads` in a direct style (including `Future` and lazy `IO`) is impossible in Scala.
despite context functions and the upcoming capture checking system. Scala lacks an underlying system such as Kotlin continuations or Java LOOM, where functions can suspend and resume computations.

### Other communities and languages

Other communities and languages concerned about ergonomics and performance, like Kotlin and Java, are bringing the notion of native support for scoped [continuations](https://kotlinlang.org/docs/composing-suspending-functions.html#async-style-functions) and structured concurrency.
These features allow for sync and async programming without boxed return types and indirect monadic style.

These languages implement such techniques in different ways. In the case of Kotlin, the compiler performs CPS transformations for `suspend` functions, eliminating the need for callbacks and simplifying function return types.
This simple native compiler supported keyword allows other ecosystem libraries such as [Spring](insert link), [Compose](insert link), and a great many other libraries and projects in the Kotlin ecosystem integrate with suspending functions natively.

JDK 19, the Java 19 hotspot runtime, and [Project Loom](https://openjdk.org/projects/loom/) include native and library support of virtual threads, structured concurrency, and [continuations](https://github.com/openjdk/loom/blob/132bc6aacdf5dfa4897e44772bc7b2052fc2f2d2/src/hotspot/share/runtime/continuation.cpp) related to virtual threads. 

## Proposal

We want to propose a native system for continuations in Scala.

Two possible implementations are included in this Pre-SIP Post:

1. The addition of a new keyword, `suspend`.
    ```scala
    suspend def countryCode: String
    ```

2. The use of compiler-desugared `Suspend` context functions or given evidence.
    ```scala
    def countryCode: Suspend ?=> String
    ```

Our intuition is that this could be part of the experimental [Capture Checking](https://www-dev.scala-lang.org/scala3/reference/experimental/cc.html) and related to the experimental [CanThrow](https://docs.scala-lang.org/scala3/reference/experimental/canthrow.html) capabilities, where the compiler performs special desugaring in the function body.

### Potential implementation

If the compiler followed a model similar to Kotlin, suspended function and lambdas get to codegen with an additional parameter.

`suspend def countryCode: String` is desugared to a function that looks like `def countryCode(continuation: Continuation[?]): Any | Null`.

The body of the suspended function desugars to a [state machine](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md#state-machines) where each state is associated with suspension points.
In the function `countryCode`, calls to `join` and `bind` are calls to suspended functions, and they are considered suspension points.
When a program reaches a suspension point the underlying continuation may have suspended, performed some work, and resumed back to the original control flow when ready.
The continuation can perform this background work without blocking the caller.

This CPS desugaring allows for user code to never have to deal with callbacks and allows for integrations with boxed types like `Future`.

    ```scala
     extension [A](f: Future[A])(using ExecutionContext) 
       suspend def join(): A = 
         continuation[A] { cont: Continuation[A] =>
            f.onComplete { 
              _.fold(ex => cont.resumeWithException(ex), cont.resume)
            }
          }
    ```

We use `continuation` to create a continuation that suspends the current program and resumes it when the future is completed.
`continuation` is only available when the user is inside the scope of a `suspend` function.
Continuations can be resumed with the expected value or an exception.

More complex abstractions and operators can be derived from this continuation model.
All is needed for direct syntax that removes callbacks and boxing is a continuation system with some form of compiler
CPS. We don't expect functions like `bind` or `join` to be available in the language, but we do expect `continuation` or similar to be available.

In the following `Example Library` we will see how we can use continuations to also implement monad bind over `Either` and `Option`.

## Example library

The following examples demonstrate the basics of a small functional library on top of the proposed continuation system, and it's inspired in primitives found
on [Arrow FX](https://arrow-kt.io/docs/fx/). `Arrow` provides complementary functional abstractions for Kotlin and KotlinX coroutines.

We don't propose this api as part of the proposal. The example is here to demonstrate what can be built on top of the continuation system.

A continuation is a program producing `A` that may be interrupted at any point with a value of `R`.
`Control` provides `shift` the operation that allows interruption and acts as a functional `throw`:

```scala
trait Control[-R]: //can also be implemented in terms of `canThrow`
  extension (r: R) 
    suspend def shift[A]: A // can throw or shift to R when otherwise expected A
```

All programs requiring `Control` are foldable:

```scala
suspend fun fold[R, A, B](
      inline program: Control[R] ?=> A
  )(inline recover: R => B, inline transform: A => B): B =
    val control = ??? //control is compile time derived or implemented in terms of `continuation`
    try {
      transform(program(using control)) 
    } catch {
      case e: Throwable =>
        handleControl(control, e)
    }
```

This is a naive impl that does not consider cancellation and shifting in nested async programs.
For a full impl with more operators and abstractions,
see [EffectScope](https://github.com/arrow-kt/arrow/blob/main/arrow-libs/core/arrow-core/src/commonMain/kotlin/arrow/core/continuations/EffectScope.kt)
and [`fold`](https://github.com/arrow-kt/arrow/blob/main/arrow-libs/core/arrow-core/src/commonMain/kotlin/arrow/core/continuations/Effect.kt#L760) impl in Arrow.

A helpful operator is monad bind. Here monad bind has the shape `F[A] => A`. Once we have `bind` we can extract `A`
without needing to `map` over `F`. If at any point we encounter a failure case we would not get `A`, and our program
short-circuits up to the nearest `Control` in the same way exceptions work.

```scala
extension [R, A](fa: Either[R, A]) 
  suspend def bind(using Control[R]): A = 
    fa.fold(_.shift, identity) //shifts on left

extension [A](fa: Option[A])
  suspend def bind(using Control[None.type]): A = 
    fa.fold(None.shift)(identity) //shifts on None
```

We can safely combine our types with `bind` in the same scope.
`shift` allows us to escape the continuation in case we encounter a `Left` or  `None`.
Monadic values compose in the same scope delegating their computation to the underlying continuation which may shift in case of failure or return the value.
There is no need for wrapped return types, monad transformers or stacked types modeling a sequential computation in this model.

Now that we have the implementations for `join` and `bind` we can express `countryCode` in direct non-blocking style.

```scala
def countryCode(maybePerson: Option[Person])(using Control[NotFound.type | None.type]): String =
  val person = maybePerson.bind //shifts on None
  val addressOrNotFound = person.address.join //throws if fails to complete (we don't want to control this)
  val address = addressOrNotFound.bind //shifts on NotFound
  val country = address.country.bind //shifts on None
  country.code.bind //shifts on None
```

Finally, we have used `using` clauses to model functions with effects or context function to model program as values with
`given` effect requirements.
The results of these programs with shape `Control[R] ?=> A` can be executed by folding a continuation.
`run` and other similar operators that fold the continuation usually look like:

```scala
extension [R, A](c: Control[R] ?=> A)

    def toEither: Either[R, A] =
      fold(c)(Left(_), Right(_))

    def toOption: Option[A] =
      fold(c)(_ => None, Some(_))

    def run: (R | A) = Continuation.fold(c)(identity, identity)
```

## Alternative community initiatives

Other projects such as [dotty-cps-async](https://github.com/rssh/dotty-cps-async) or [Monadless](https://github.com/monadless/monadless) 
provider similar syntactic sugar for monads and do a great job about it. 
We have enjoyed using these libraries but after trying native language support for these features in Kotlin we decided to propose a deeper integration that works over declarations and not just expressions.

## Improvements

We believe that introducing continuations in Scala 3 coupled or not to the capture checking system or context function results in the following improvements:

- Simplifies program description, eliminating wrapped return types for most use cases.
- Helps inference and compile times due to reducing the usage of complex types.
- Cooperates with the language control structures and produces smaller and faster programs that desugar suspension points efficiently in the stack.
- Eases the learning curve to program async / effects-based applications and libraries in Scala.
- Reduces indirection through higher-order functions used extensively in `map`, `flatMap`, and others.
- Can interop with other libraries and frameworks that offer custom fiber scheduling and cancellation strategies.

Looking forward to your thoughts and feedback, thank you for reading this far! :pray:

