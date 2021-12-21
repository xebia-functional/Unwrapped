package fx.internal

import fx.internal.LockFreeLinkedList.Node

class PrepareOp(
    val affected: Node,
    val next: Node,
    val desc: AbstractAtomicDesc
) extends AtomicOpDescriptor:
  override val atomicOp: AtomicOp[?] = desc.atomicOp

  // Returns REMOVE_PREPARED or null (it makes decision on any failure)
  override def perform(affected: Any | Null): Any | Null =
    assert { affected == this.affected }
    val af = affected.asInstanceOf[Node] // type assertion
    val decision = desc.onPrepare(this)
    if (decision == RemovePrepared) {
      // remove element on failure -- do not mark as decided, will try another one
      val next = this.next
      val removed = next.removed()
      if (af._next.compareAndSet(this, removed)) {
        // The element was actually removed
        desc.onRemoved(af)
        // Complete removal operation here. It bails out if next node is also removed and it becomes
        // responsibility of the next's removes to call correctPrev which would help fix all the links.
        next.correctPrev(null)
      }
      return RemovePrepared
    }
    // We need to ensure progress even if it operation result consensus was already decided
    val consensus = if (decision != null) {
      // some other logic failure, including RETRY_ATOMIC -- reach consensus on decision fail reason ASAP
      atomicOp.decide(decision)
    } else {
      atomicOp.consensus // consult with current decision status like in Harris DCSS
    }
    val update: Any =
      if (consensus == Undecided)
        atomicOp // desc.onPrepare returned null -> start doing atomic op
      else if (consensus == null)
        desc.updatedNext(af, next) // move forward if consensus on success
      else next // roll back if consensus if failure

    af._next.compareAndSet(this, update)
    null

  def finishPrepare(): Unit = desc.finishPrepare(this)
