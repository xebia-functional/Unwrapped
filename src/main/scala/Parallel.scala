package fx

import scala.annotation.implicitNotFound
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.CompletableFuture
import java.util.Collection
import scala.jdk.CollectionConverters.*
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.StructuredExecutor
import java.time.Instant

@implicitNotFound(
  "parallel requires capability:\n* Parallel"
)
opaque type Parallel = Unit

object Parallel:
  given Parallel = ()

inline def parallel[X <: Tuple](
    inline f: X
)(using
    TupledVarargs[Function0, X]#Args =:= X
): TupledVarargs[Function0, X]#Result = structured({
  val results =
    f.toList
      .map(fa => fa.asInstanceOf[() => Any])
      .map(fork(_))
  join
  Tuple
    .fromArray(results.map(_.get).toArray)
    .asInstanceOf[TupledVarargs[Function0, X]#Result]
})

inline def parallelMap[X <: Tuple, C](
    inline f: X,
    fc: (TupledVarargs[Function0, X]#Result) => C
)(using
    TupledVarargs[Function0, X]#Args =:= X
): C = structured({
  val results =
    f.toList
      .map(fa => fa.asInstanceOf[() => Any])
      .map(fork(_))
  join
  fc(Tuple
    .fromArray(results.map(_.get).toArray)
    .asInstanceOf[TupledVarargs[Function0, X]#Result])
})