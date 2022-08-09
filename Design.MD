## Use cases
### Async programming
### Generators
## Implementation
### Api
#### Public

```scala
trait Continuation[A]
  val context: ExecutionContext
  def resume(value: A): Unit
  def resumeWithException(error: Throwable): Unit
```

#### Internal

Some notes studying the Kotlin implementation below.

##### Continuation API

Continuation has two methods, `resume` and `resumeWithException`.
The first one is to return a _suspended value_,
and the latter is to _bridge_ an async exception to caller thread.
Kotlin uses `Result<A>` to merge both methods into 1, but it causes additional overhead and complexity.

```scala
trait Continuation[A]
  def resume(value: A): Unit
  def resumeWithException(error: Throwable): Unit
```

##### Interception

Kotlin uses `ContinuationIntercepted`, and `Continuation#intercepted` for dispatching.
The reason for this is that `Continuation` exists in Kotlin Std, but the concurrency implementation lives inside `KotlinX Coroutines`.
The Kotlin Standard Library has _no concept_ of concurrency **at all**, so no abstraction such as `ExecutionContext` is available.

Using `ExecutionContext` for the Scala implementation makes more sense, so we can replace `CoroutineContext` with `ExecutionContext`.
`CoroutineContext` is further utilised to implement things such as `CoroutineContextLocal` alla `ThreadContextLocal`,
and storing other information inside such as `CoroutineName` for debugging purposes so we can still maintain some `State` in a `CoroutineContext` like type.
We will however not use it for _dispatching_ or scheduling for that we prefer to use `ExecutionContext` in Scala.

```scala
trait Continuation[A]
  val context: ExecutionContext
```

##### Coroutine Builders

Kotlin splits up the builders in `suspendCoroutine`, and `suspendCoroutineCancellable`.
**Again** because Kotlin Std has no notion of concurrency, and thus no-notion of cancellation.
We should probably not make this distinction/split in Scala, and prefer to have `suspendCoroutineCancellable` only.

The coroutine builders should still rely on `SafeContinuation` and `intercepted`:
1. `SafeContinuation` is a way of racing "foreign wrapping code" with the coroutine state machine.
   When wrapping foreign code, SDKs often implicitly switch Thread, and thus we need to race to figure out if we had an _immediate_ or `COROUTINE_SUSPENDED` return.
2. For _safety,_ we should _preserve the caller context_, and thus jump back to it. Otherwise, we also risk occupying SDK threads.

=> SafeContinuation uses a `Undecided, Suspended & Resumed` enum to track state in Kotlin,
but in Scala we can just use `val result: T | Undecided | Continuation.Suspended = Undediced`.
The `Resumed` one is not needed, and the `Continuation.Suspended` already exists.

Low-level builders (TBD).

- suspendCoroutineUninterceptedOrReturn `val p1: completion`
- startCoroutineUninterceptedOrReturn `(this as Function<Continuation<Any?>, Any?>).invoke(completion)`
- createCoroutineUnintercepted (do we really need this?)

For efficiency, we want to have `suspendCoroutineUninterceptedOrReturn` so you can return immediate values from `val x: Suspend %=> Int = 1`.
This way the CPS compiler can also optimise these functions, and make them perform as immediate/regular functions inside the `Suspend` machinery.

### Context functions
### State machines
### Resumption
### CPS gen lambdas
#### Tail call suspension / optimization
#### Inlining and boxing
### Spilling Variables
#### Naming
#### Clean up
### Multi platform support
#### Scala JVM
#### Scala JS
#### Scala Native
### Debugging