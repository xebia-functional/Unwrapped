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
    Tasks[X] =:= X
): TasksResults[X] = structured({
  val tasks = f.toList
    .map(fa => callableOf[Any](fa.asInstanceOf[() => Any]))
    .asJava
  val results =
    f.toList
      .map(fa => callableOf[Any](fa.asInstanceOf[() => Any]))
      .map(fork(_))

  //join


  Tuple
    .fromArray(results.map(_.get).toArray)
    .asInstanceOf[TasksResults[X]]
})

type IsTask[A] <: Boolean = A match
  case (() => ?) => true
  case _         => false

type Tasks[X <: Tuple] = Tuple.Filter[X, IsTask]

type UnkindedTuple[F[_], T <: Tuple] <: Tuple =
  T match
    case EmptyTuple => EmptyTuple
    case F[a] *: t  => a *: UnkindedTuple[F, t]

type TasksResults[T <: Tuple] = UnkindedTuple[Function0, T]
