import arrow.core.identity
import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.raceN
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

data class AuthToken(val expiresAt: Instant, val value: String) {
  fun isActive(now: Instant): Boolean = now.isBefore(expiresAt)
}

// loops `f` forever until error or cancellation
fun CoroutineScope.forever(f: suspend () -> Unit): Job =
  launch {
    while (true) {
      ensureActive() // IO.cancelBoundary
      f()
    }
  }

// TODO we could abstract over `Atomic`, and plug-in a persistent cache
/** Actor will live as long as the [CoroutineScope] */
fun <S, O> CoroutineScope.actor(
  initialState: S,
  receive: suspend Atomic<S>.() -> O
): suspend () -> O {
  val atomic = Atomic.unsafe(initialState)
  val channel = Channel<CompletableDeferred<O>>()
  val job = forever {
    val promise = channel.receive()
    val output = receive(atomic)
    promise.complete(output)
  }
  return suspend {
    val promise = CompletableDeferred<O>()
    channel.send(promise)
    val output = raceN({ job.join() }, { promise.await() })
      .fold({ throw RuntimeException("Scope closed.") }, ::identity)
    output
  }
}

fun <S, I, O> CoroutineScope.actorWithInput(
  initialState: S,
  receive: suspend (I, Atomic<S>) -> O
): suspend (I) -> O {
  val atomic = Atomic.unsafe(initialState)
  val channel = Channel<Pair<I, CompletableDeferred<O>>>()
  val job = forever {
    val (input, promise) = channel.receive()
    val output = receive(input, atomic)
    promise.complete(output)
  }
  return { i: I ->
    val promise = CompletableDeferred<O>()
    channel.send(Pair(i, promise))
    val output = raceN({ job.join() }, { promise.await() })
      .fold({ throw RuntimeException("Scope closed.") }, ::identity)
    output
  }
}

fun CoroutineScope.requestActiveAuthToken(
  clock: Clock = Clock.Default
): suspend () -> AuthToken =
  actor<AuthToken?, AuthToken>(initialState = null) {
    val existingToken = get()
    val now = clock.now()
    existingToken
      ?.takeIf { it.isActive(now) } ?: requestNewAuthToken(clock).also { set(it) }
  }

private suspend fun requestNewAuthToken(clock: Clock = Clock.Default): AuthToken =
  AuthToken(clock.now().plusSeconds(3600), "token")

suspend fun main() {
  val scope = CoroutineScope(Dispatchers.Default)

  val store = scope.requestActiveAuthToken()

  val activeAuthToken = store.invoke()
  println(activeAuthToken)

  scope.cancel()
}
