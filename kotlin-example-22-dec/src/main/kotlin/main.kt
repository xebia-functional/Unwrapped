import arrow.core.Either
import arrow.core.computations.either
import arrow.core.right
import arrow.fx.coroutines.Resource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine

typealias IO<A> = suspend () -> A

val io: IO<Int> = ::example
val suspend: suspend () -> Int = ::example

suspend fun example(): Int = 1

fun <A, B> IO<A>.flatMap(f: (A) -> IO<B>): IO<B> = {
  val a = invoke()
  val ioB = f(a)
  ioB.invoke()
}

val x: IO<String> = io.flatMap { suspend { it.toString() } }

suspend fun x() = example().toString()

suspend fun emit2(i: Int): Unit =
  suspendCoroutine { cont ->
    cont.resume(Unit)
  }

suspend fun cancallable(): Unit =
  suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation {
      // release handlers
    }
  }

suspend fun traverse(): List<Unit> =
  listOf(1, 2, 3).map { cancallable() }

suspend fun example2(): Either<String, List<Unit>> = either {
  listOf(1, 2, 3).map {
    cancallable().right().bind()
  }
}
// Pull<Int, Unit>
suspend fun FlowCollector<Int>.pullProgram(): Unit {
  var count = 0
  while(true) {
    emit(count++)
  }
}

val infinite: Flow<Int> = flow { pullProgram() }

suspend fun main() {
  val flow = infinite
  flow.collect { i: Int ->
    println("consuming $i")
  }
}

fun <A> Resource<A>.asFlow(): Flow<A> = flow {
  use { a -> emit(a) }
}

