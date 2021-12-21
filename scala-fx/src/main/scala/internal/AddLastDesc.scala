package fx.internal

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.annotation.targetName
import scala.util.control.Breaks._
import fx.internal.LockFreeLinkedList.Node

class AddLastDesc[T <: Node](
    val queue: Node,
    val node: T
) extends AbstractAtomicDesc {
  assert { node._next.get == node && node._prev.get == node }

  // Returns null when atomic op got into deadlock trying to help operation that started later (RETRY_ATOMIC)
  override def takeAffectedNode(op: AtomicOpDescriptor): Node | Null =
    queue.correctPrev(
      op
    ) // queue head is never removed, so null result can only mean RETRY_ATOMIC

  private val _affectedNode: AtomicReference[Node | Null] = AtomicReference(null)
  val affectedNode: Node | Null = _affectedNode.get
  val originalNext: Node = queue

  override def retry(affected: Node, next: Any): Boolean = next != queue

  override def finishPrepare(prepareOp: PrepareOp) =
    // Note: onPrepare must use CAS to make sure the stale invocation is not
    // going to overwrite the previous decision on successful preparation.
    // Result of CAS is irrelevant, but we must ensure that it is set when invoker completes
    _affectedNode.compareAndSet(null, prepareOp.affected)

  override def updatedNext(affected: Node, next: Node): Any =
    // it is invoked only on successfully completion of operation, but this invocation can be stale,
    // so we must use CAS to set both prev & next pointers
    node._prev.compareAndSet(node, affected)
    node._next.compareAndSet(node, queue)
    node

  override def finishOnSuccess(affected: Node, next: Node) =
    node.finishAdd(queue)

}