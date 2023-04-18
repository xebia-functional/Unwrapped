package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def GenericArgumentsContinuations =
  def genericArgumentsContinuations[A](x: A)(using Suspend): Int =
    summon[Suspend].shift[Unit] { continuation =>
      continuation.resume(println(x))
    }
    summon[Suspend].shift[Int] { continuation => continuation.resume(2) }

  println(SuspendApp(genericArgumentsContinuations(1)))
