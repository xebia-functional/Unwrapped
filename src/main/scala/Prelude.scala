package fx

trait TupledVarargs[F[_], X <: Tuple]:

  type ArgsFilter[X] <: Boolean =
    X match
      case F[?] => true
      case _    => false

  type Args = Tuple.Filter[X, ArgsFilter]

  type Result = Tuple.InverseMap[X, F]

