package examples

import continuations.Suspend

@main def GenericArgumentsContinuations =
  def genericArgumentsContinuations[A](x: A)(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(2) }

  println(genericArgumentsContinuations(1))
