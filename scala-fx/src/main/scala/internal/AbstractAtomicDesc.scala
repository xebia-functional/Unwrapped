package fx.internal

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.annotation.targetName
import scala.util.control.Breaks._
import fx.internal.LockFreeLinkedList.Node

abstract class AbstractAtomicDesc extends AtomicDesc {
  protected val affectedNode: Node | Null
  protected val originalNext: Node | Null
  protected def takeAffectedNode(op: AtomicOpDescriptor): Node | Null =
    affectedNode // null for RETRY_ATOMIC
  protected def failure(affected: Node): Any | Null = null // next: Node | Removed
  protected def retry(affected: Node, next: Any): Boolean = false // next: Node | Removed
  protected def finishOnSuccess(affected: Node, next: Node): Unit

  def updatedNext(affected: Node, next: Node): Any

  def finishPrepare(prepareOp: PrepareOp): Unit

  // non-null on failure
  def onPrepare(prepareOp: PrepareOp): Any | Null =
    finishPrepare(prepareOp)
    null

  def onRemoved(affected: Node) = () // called once when node was prepared & later removed

  def prepare(op: AtomicOp[?]): Any | Null =
    while (true) { // lock free loop on next
      breakable {
        val affected = takeAffectedNode(op)
        if (affected == null) return RetryAtomic
        // read its original next pointer first
        val next = affected._next.get
        // then see if already reached consensus on overall operation
        if (next == op) return null // already in process of operation -- all is good
        if (op.isDecided) return null // already decided this operation -- go to next desc
        if (next.isInstanceOf[AtomicOpDescriptor]) {
          val n = next.asInstanceOf[AtomicOpDescriptor]
          // some other operation is in process
          // if operation in progress (preparing or prepared) has higher sequence number -- abort our preparations
          if (op.isEarlierThan(n))
            return RetryAtomic
          n.perform(affected)
          break // and retry
        }
        // next: Node | Removed
        val fail = failure(affected)
        if (fail != null) return fail // signal failure
        if (retry(affected, next)) break // retry operation
        val prepareOp = PrepareOp(affected, next.asInstanceOf[Node], this)
        if (affected._next.compareAndSet(next, prepareOp)) {
          // prepared -- complete preparations
          try
            val prepFail = prepareOp.perform(affected)
            if (prepFail == RemovePrepared) break // retry
            assert { prepFail == null }
            return null
          catch
            case e: Throwable =>
              // Crashed during preparation (for example IllegalStateExpception) -- undo & rethrow
              affected._next.compareAndSet(prepareOp, next)
              throw e

        }
      }
    }

  def complete(op: AtomicOp[?], failure: Any | Null) =
    val success = failure == null
    if (affectedNode == null) assert { !success }; return
    if (originalNext == null) assert { !success }; return
    val update = if (success) updatedNext(affectedNode, originalNext) else originalNext
    if (affectedNode._next.compareAndSet(op, update)) {
      if (success) finishOnSuccess(affectedNode, originalNext)
    }

}