# scala-fx

## Getting started

Scala-fx is an effects library for Scala 3 that introduces structured concurrency and an abilities system to describe pure functions and programs. 

The example below is a pure program that returns `Int` and requires the context capability `Bind`. Bind enables the `bind` syntax over values of Either and other types.

```scala mdoc:reset
import fx.*

val program: Int =
    Right(1).bind + Right(2).bind
```

Using Scala3 features such as context functions we can encode pure programs in terms of capabilities with minimal overhead.
Capabilities can be introduced a la carte and will be carried as given contextual evidences through call sites until you proof you can get rid of them.

```scala mdoc:reset
import fx.*

def runProgram: Int | String =
    val program: Errors[String] ?=> Int =
      Right(1).bind + Right(2).bind + "oops".raise[Int]

    run(program)

println(runProgram)
```

Users and library authors may define their own Capabilities. Here is how `Bind` for `Either[E, A]` is declared

```scala
/** Brings the capability to perform Monad bind in place. Types may
  * access [[Control]] to short-circuit as necessary
  */
extension [R, A](fa: Either[R, A])
  def bind: Errors[R] ?=> A = fa.fold(_.shift, identity)
```

Scala Fx supports a structured concurrency model backed by the non-blocking [StructuredExecutorTask](https://openjdk.java.net/jeps/428)
where you can `fork` and `join` cancellable fibers and scopes.

Popular functions like `parallel` support arbitrary typed arity in arguments and return types.

```scala mdoc:reset
import fx.*

def runProgram: (String, Int, Double) =
  val results: Structured ?=> (String, Int, Double) =
    parallel(
      () => "1",
      () => 0,
      () => 47.03
    )

  structured(results)

println(runProgram)
```

Continuations based on Control Throwable or a non-blocking model like Loom are useful because they allow us to intermix async and sync programs in the same syntax without the need for boxing as is frequently the case in most scala effect libraries.

### Build and run in your local environment:

Pre-requisites:

- [Project Loom](https://jdk.java.net/loom/)
- Scala 3

1. Download the latest Project Loom [Early-Access build](https://jdk.java.net/loom/) for your system architecture.
2. Set your `JAVA_HOME` to the path you extracted above.

You can now compile and run the tests:

```shell
env JAVA_OPTS='--add-modules jdk.incubator.concurrent' sbt "clean; compile; test"
```

**NOTE**: The Loom project is defined as an incubator module that is a means to distribute APIs which are not final or completed to get feedback from the developers.
You should include the `-add-module` Java option to add the module to the class path of the project.
