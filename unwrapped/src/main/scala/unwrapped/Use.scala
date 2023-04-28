package unwrapped

import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException

inline def runReleaseAndRethrow(
    inline original: Throwable,
    inline f: () => Unit
): Nothing =
  try {
    uncancellable(f)
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
  structured(uncancellable(finalizer))
  res

inline def guaranteeCase[A](
    inline fa: () => A,
    inline finalizer: (ExitCase) => Unit
): A =
  val res =
    try fa()
    catch
      case (e: CancellationException) =>
        runReleaseAndRethrow(e, () => finalizer(ExitCase.Cancelled(e)))
      case (e: ExecutionException) =>
        runReleaseAndRethrow(
          e.getCause,
          () => finalizer(ExitCase.Cancelled(e.getCause))
        )
      case NonFatal(t) =>
        runReleaseAndRethrow(t, () => finalizer(ExitCase.Failure(t)))
  structured(uncancellable(() => finalizer(ExitCase.Completed)))
  res

inline def bracket[A, B](
    inline acquire: () => A,
    inline use: (A) => B,
    inline release: (A) => Unit
): B =
  val acquired = uncancellable(acquire)
  val res =
    try use(acquired)
    catch
      case (e: CancellationException) =>
        runReleaseAndRethrow(e, () => release(acquired))
      case (e: ExecutionException) =>
        runReleaseAndRethrow(e.getCause, () => release(acquired))
      case NonFatal(t) =>
        runReleaseAndRethrow(t, () => release(acquired))
  structured(uncancellable(() => release(acquired)))
  res

inline def bracketCase[A, B](
    inline acquire: () => A,
    inline use: (A) => B,
    inline release: (A, ExitCase) => Unit
): B =
  val acquired = uncancellable(acquire)
  val res =
    try use(acquired)
    catch
      case (e: CancellationException) =>
        runReleaseAndRethrow(e, () => release(acquired, ExitCase.Cancelled(e)))
      case (e: ExecutionException) =>
        runReleaseAndRethrow(
          e.getCause,
          () => release(acquired, ExitCase.Cancelled(e.getCause))
        )
      case NonFatal(t) =>
        runReleaseAndRethrow(t, () => release(acquired, ExitCase.Failure(t)))
  uncancellable(() => release(acquired, ExitCase.Completed))
  res
