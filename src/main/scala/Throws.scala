package fx

import scala.annotation.implicitNotFound

// TODO this can `erased` with scala3 still experimental erased feature
@implicitNotFound(
  "Missing capability:\n" +
    "* Throws[${R}]\n" +
    "alternatively you may resolve this call with \n" +
    "```scala\nhandle(f)(recover)\n```\n" +
    "or\n ignore thrown exceptions with import fx.unsafe.unsafeExceptions`"
)
opaque type Throws[-R <: Exception] = Unit

object Throws :
  given unsafeExceptions[R <: Exception]: Throws[R] = ()

inline def handle[R <: Exception, A](
    inline f: A % Throws[R]
)(inline recover: (R) => A): A =
  try
    import fx.Throws.unsafeExceptions
    f
  catch
    case r: R =>
      recover(r)
