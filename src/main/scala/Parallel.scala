package fx

import scala.annotation.implicitNotFound
import Tuple.{Map, IsMappedBy, InverseMap}
import scala.annotation.tailrec
import fx.Tuples.mapK

type ParTupled = [F[_], X <: Tuple] =>> MapK[Fiber, Id, MapK[F, Fiber, X]]

type ParBind = [F[_]] =>> [t] => F[t] => t

given idParBind: ParBind[Id] =
  [t] => (f: Id[t]) => f

given function0ParBind: ParBind[Function0] =
  [t] => (f: Function0[t]) => f.apply

given resourceParBind: ParBind[Resource] =
  [t] => (f: Resource[t]) => f.bind

extension [X <: Tuple](x: X)
  def par[F[_]](using
      IsMappedBy[F][X],
      ParBind[F]
  ): ParTupled[F, X] * Structured =
    val fibers =
      x.mapK[F, Fiber](
        [r] => (r: F[r]) => fork(() => summon[ParBind[F]](r))
      )
    joinAll
    val awaited =
      fibers.mapK[Fiber, Id](
        [r] => (r: Fiber[r]) => r.join
      )
    awaited

@main def ParallelFunctionExample =
  val results: (String, Int, Double) * Structured =
    (
      () => "1",
      () => 0,
      () => 47.03
    ).par[Function0]
  println(structured(results))

@main def ParallelResourceExample =
  val results: (Int, String, Double) * Structured =
    (Resource(1), Resource("a"), Resource(47.0)).par[Resource]

  println(structured(results))
