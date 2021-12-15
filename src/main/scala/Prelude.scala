package fx

import Tuple.{InverseMap, IsMappedBy, Map}
import scala.annotation.showAsInfix

type Id[+A] = A

/**
 * Maps `(F[T1], ..., F[Tn])` to `(G[T1], ..., G[Tn])`
 */
type MapK[F[_], G[_], X <: Tuple] =
  Map[InverseMap[X, F], G]

object Tuples:
  extension [X <: Tuple](x: X)
    /**
     * Converts a tuple `(F[T1], ..., F[Tn])` to `(G[T1], ..., G[Tn])`
     */
    def mapK[F[_], G[_]](
        map: [t] => (ft: F[t]) => G[t]
    ): MapK[F, G, X] =
      x.map[G]([t] => (t: t) => map(t.asInstanceOf[F[t]])).asInstanceOf[MapK[F, G, X]]
