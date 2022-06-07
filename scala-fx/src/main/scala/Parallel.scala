package fx

import scala.annotation.{implicitNotFound, tailrec, targetName}
import fx.Id

import scala.Tuple.{InverseMap, IsMappedBy}

type ParBind[F[_]] = [t] => F[t] => t

private type Id[+A] = A

def par[F[_], X <: Tuple](
    x: X, parBind: ParBind[F])(using IsMappedBy[F][X], Structured): InverseMap[X, F] % Structured =
  val fibers =
    x.map[Fiber](
      [r] => (r: r) => fork(() => parBind(r.asInstanceOf[F[r]]))
    )
  joinAll
  val awaited =
    fibers.map[Id](
      [r] => (r: r) => r.asInstanceOf[Fiber[r]].join
    )
  awaited.asInstanceOf[InverseMap[X, F]]

extension [X <: Tuple](x: X)(using IsMappedBy[Function0][X], Structured)
  def parallel: InverseMap[X, Function0] =
    par(x, [t] => (f : () => t) => f.apply)

@main def ParallelFunctionExample =
  val results: (String, Int, Double) % Structured =
    parallel(
      () => "1",
      () => 0,
      () => 47.03
    )
  println(structured(results))

