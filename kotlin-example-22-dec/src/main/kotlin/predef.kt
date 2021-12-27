@file:OptIn(ExperimentalTime::class)

import arrow.fx.coroutines.timeInMillis
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimedValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

suspend fun <A> FlowCollector<A>.emitAll(s: Sequence<A>): Unit =
  s.forEach { emit(it) }

/**
 * Inserts a [separator] in between every element.
 *
 * ```kotlin
 * flowOf(1, 2, 3, 4, 5)
 *   .intersperse(0)
 *   .toList() shouldBe listOf(1, 0, 2, 0, 3, 0, 4, 0, 5)
 * ```
 */
fun <A> Flow<A>.intersperse(separator: A): Flow<A> =
  flow {
    var first = true
    collect {
      if (first) {
        emit(it)
        first = false
      } else {
        emit(separator)
        emit(it)
      }
    }
  }

typealias Processor<A, B> = (Flow<A>) -> Flow<B>

/** An inline operator that applies a [Processor] to a [Flow] in a fluent API.  */
inline fun <A, B> Flow<A>.through(processor: Processor<A, B>): Flow<B> = processor(this)

interface Clock : Real, Monotonic {
  companion object {
    val Default: Clock = object : Clock,
      Real by Real.Default,
      Monotonic by Monotonic.Default {}
  }
}

fun interface Monotonic {
  suspend fun epochMillis(): Long

  companion object {
    val Default = Monotonic { timeInMillis() }
  }
}

fun interface Real {
  suspend fun nanoTime(): Long

  suspend fun <A> timed(f: suspend () -> A): TimedValue<A> {
    val start = nanoTime()
    return TimedValue(f(), Duration.nanoseconds(nanoTime() - start))
  }

  companion object {
    val Default = Real { System.nanoTime() }
  }
}
