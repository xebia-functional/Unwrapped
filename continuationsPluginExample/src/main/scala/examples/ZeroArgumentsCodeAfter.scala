package examples

import continuations.Suspend

@main def ZeroArgumentsCodeAfter =
  def zeroArgumentsCodeAfter()(using Suspend): Int =
    shift[Unit] { continuation => continuation.resume(println(1)) }
    shift[Unit] { continuation => continuation.resume(println(2)) }
    3
  println(zeroArgumentsCodeAfter())
