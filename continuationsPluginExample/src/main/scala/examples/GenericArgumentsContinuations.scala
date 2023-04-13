package examples

import continuations.Suspend

@main def GenericArgumentsContinuations =
  def genericArgumentsContinuations[A](x: A)(using Suspend): Int =
    shift[Unit] { continuation => continuation.resume(println(x)) }
    shift[Int] { continuation => continuation.resume(2) }

  println(genericArgumentsContinuations(1))
