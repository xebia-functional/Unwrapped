@file:OptIn(FlowPreview::class)

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.fromCloseable
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.useLines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

fun fahrenheitToCelsius(f: Double): Double =
  (f - 32.0) * (5.0 / 9.0)

fun fahrenheitToCelsius(line: String): Double? =
  line.toDoubleOrNull()?.let(::fahrenheitToCelsius)

/** Files API for streaming */
object Files {
  /** readAll lines from [Path] */
  fun readAll(path: Path): Flow<String> = flow {
    path.useLines { lines -> emitAll(lines) }
  }

  /** A transformation that writes all [ByteArray] to [Path] */
  fun writeAll(path: Path): (Flow<ByteArray>) -> Flow<Unit> = { byteFlow ->
    Resource.fromCloseable { path.toFile().outputStream() }
      .asFlow()
      .flatMapConcat { writer ->
        byteFlow.map { writer.write(it) }
      }.flowOn(Dispatchers.IO)
  }
}

// Alternative encoding for `through` and `Processor`
fun Flow<ByteArray>.writeAll(path: Path): Flow<Unit> =
  through(Files.writeAll(path))

suspend fun main(): Unit {
  Files.readAll(Path("testdata/fahrenheit.txt"))
    .filter { line -> line.trim().isNotEmpty() && !line.startsWith("//") }
    .mapNotNull { line -> fahrenheitToCelsius(line)?.toString() }
    .intersperse("\n")
    .map { it.toByteArray() }
    .through(Files.writeAll(Path("testdata/celcius.txt")))
    .collect()

  val averageCelcius =
    Files.readAll(Path("testdata/fahrenheit.txt"))
      .mapNotNull(::fahrenheitToCelsius)
      .average()

  println(averageCelcius)
}

suspend fun Flow<Double>.average(): Double =
  fold(Pair(0.0, 1.0)) { (total, size), acc ->
    Pair((total + acc) / size, size + 1)
  }.first
