package fx

import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException

inline def runReleaseAndRethrow(
    inline original: Throwable,
    inline f: () => Unit
): Nothing =
  try {
    uncancellable(f).apply
  } catch
    case NonFatal(e) =>
      original.addSuppressed(e)
  throw original

inline def guarantee[A](
    inline fa: () => A,
    inline finalizer: () => Unit
): A =
  val res =
    try fa()
    catch
      case (e: CancellationException) =>
        runReleaseAndRethrow(e, finalizer)
      case (e: ExecutionException) =>
        runReleaseAndRethrow(e.getCause, finalizer)
      case NonFatal(t) =>
        runReleaseAndRethrow(t, finalizer)
  uncancellable(finalizer).apply
  res

inline def bracket[A, B](
    inline acquire: () => A,
    inline use: (A) => B,
    inline release: (A) => Unit
): B =
  val acquired = uncancellable(acquire).apply
  val res =
    try use(acquired)
    catch
      case (e: CancellationException) =>
        runReleaseAndRethrow(e, () => release(acquired))
      case (e: ExecutionException) =>
        runReleaseAndRethrow(e.getCause, () => release(acquired))
      case NonFatal(t) =>
        runReleaseAndRethrow(t, () => release(acquired))
  uncancellable(() => release(acquired)).apply
  res
