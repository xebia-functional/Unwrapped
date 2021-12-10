package fx

import scala.annotation.implicitNotFound

// TODO this can `erased` with scala3 still experimental erased feature
@implicitNotFound(
  "Missing capability:\n" +
    "* Throws[${R}]\n" +
    "alternatively you may resolve this call with \n" +
    "```scala\nhandle(f)(recover)\n```\n" +
    "or\n ignored thrown exceptions with import fx.unsafe.unsafeExceptions`"
)
sealed trait Throws[-R <: Exception]
private[fx] object Handled extends Throws[Nothing]

inline def handle[R <: Exception, A](
    inline f: A * Throws[R]
)(inline recover: (R) => A): A =
  try
    import fx.unsafe.unsafeExceptions
    f
  catch
    case r: R =>
      recover(r)
