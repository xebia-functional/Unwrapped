# scala-fx

## Getting started

Scala-fx is an effects library for Scala 3 that introduces structured concurrency and an abilities system to describe pure functions and programs. 

The example below is a pure program that returns `Int` and requires the context capability `Bind`. Bind enables the `bind` syntax over values of Either and other types.

```scala
val program: Int * Bind =
  Right(1).bind + Right(2).bind
```

Using Scala3 features such as context functions, infix types and erasable definitions we can can encode pure programs in terms of capabilities with minimal overhead.
Capabilities can be introduced a la carte and will be carried as given contextual evidences through call sites until you proof you can get rid of them.

```scala
val program: Int 
  * Bind 
  * Errors[String] =
  Right(1).bind + Right(2).bind + "oops".raise[Int] 

val x: Int = program 
// e: this function may shift control to String and requires capability:
//    * Control[String]
```

Users and library authors may define their own Capabilities. Here is how `Bind` for `Either[E, A]` is declared

```scala
/** Brings the capability to perform Monad bind in place. Types may
  * access [[Control]] to short-circuit as necessary
  *
  * ```scala
  * import fx.Bind
  *
  * extension [R, A](fa: Either[R, A])
  *   def bind: A * Bind * Control[R] = fa.fold(_.shift, identity)
  * ```
  */
@implicitNotFound(
  "Monadic bind requires capability:\n* Bind"
)
opaque type Bind = Unit

object Bind:
  given Bind = ()

extension [R, A](fa: Either[R, A])
  def bind: A * Bind * Errors[R] = fa.fold(_.shift, identity)
```

Scala Fx supports a structured concurrency model backed by the non blocking [StructuredExecutor](https://download.java.net/java/early_access/loom/docs/api/java.base/java/util/concurrent/StructuredExecutor.html)
where you can `fork` and `join` cancellable fibers and scopes.

Popular functions like `parallel` support arbitrary typed arity in arguments and return types.

```scala
// infers to TupledVarargs[[R] =>> () => R, (() => String, () => Int, () => Double)]#Result
val results = parallel(
    (
      () => "1",
      () => 0,
      () => 47.0
    )
  )
```

Continuations based on Control Throwable or a non blocking model like Loom are useful because they allow us to intermix async and sync programs in the same syntax without the need for boxing as is frequently the case in most scala effect libraries.

### Build and run in your local environment:

Pre-requisites:

- [Project Loom](https://jdk.java.net/loom/)
- Scala 3

1. Download the latest Project Loom [Early-Access build](https://jdk.java.net/loom/) for your system architecture.
2. Set your `JAVA_HOME` to the path you extracted above.

You can now compile and run the tests:

```shell
sbt "clean; compile; test"
```
