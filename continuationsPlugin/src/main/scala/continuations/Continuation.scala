package continuations

import continuations.jvm.internal.{BaseContinuationImpl, ContinuationImpl}

trait Continuation[-A]:
  type Ctx <: Tuple
  def context: Ctx
  def resume(value: Either[Throwable, A]): Unit
  def contextService[T](): T | Null =
    context.toList.find(_.isInstanceOf[T]).map(_.asInstanceOf[T]).orNull

object Continuation:
  enum State:
    case Suspended, Undecided, Resumed

abstract class RestrictedContinuation(
    completion: Continuation[Any | Null] | Null
) extends BaseContinuationImpl(completion):

  if (completion != null)
    require(completion.context == EmptyTuple)

  override type Ctx = EmptyTuple
  override val context: EmptyTuple = EmptyTuple

object TestContinuation:
  private def ci: ContinuationInterceptor = new ContinuationInterceptor {
    def interceptContinuation[T](continuation: Continuation[T]): Continuation[T] =
      continuation
  }

  private def c: Continuation[Any | Null] = new Continuation[Any | Null] {
    type Ctx = (ContinuationInterceptor, ContinuationInterceptor)

    def resume(value: Either[Throwable, Any | Null]): Unit =
      println("YES")

    override def context: Ctx = (ci, ci)
  }

  def contImpl: ContinuationImpl = new ContinuationImpl(c, (ci, ci)) {
    protected def invokeSuspend(
        result: Either[Throwable, Any | Null | Continuation.State.Suspended.type]): Any | Null =
      result.fold(t => throw t, or => or)
  }
