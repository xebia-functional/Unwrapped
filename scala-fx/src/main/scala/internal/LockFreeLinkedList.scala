package fx.internal

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.annotation.targetName
import scala.util.control.Breaks._
import fx.internal.LockFreeLinkedList.Node

extension [A](ref: AtomicReference[A])
  def loop(action: (A) => Unit): Nothing =
    while (true) action(ref.get)
    throw RuntimeException("impossible")

/**
 * LockFree List impl based on
 * https://github.com/Kotlin/kotlinx.coroutines/blob/a4ae389503cc9b3c940e5d5539fe359b5e4a4950/kotlinx-coroutines-core/concurrent/src/internal/LockFreeLinkedList.kt#L63
 * TODO add LICENSE if we use this prototype internal impl in the stream channels.
 */
object LockFreeLinkedList:

  enum State:
    case Undecided
    case Success
    case Failure

  class Node:
    private[internal] val _next: AtomicReference[Any] = AtomicReference(this)
    private[internal] val _prev: AtomicReference[Node] = AtomicReference(this)
    private[internal] val _removedRef: AtomicReference[Removed | Null] = AtomicReference(null)

    class Removed(val ref: Node)

    extension (value: Any)
      private[fx] def unwrap(): Node =
        value match
          case r: Removed => r.ref
          case _ => value.asInstanceOf[Node]

    def isRemoved: Boolean = next.isInstanceOf[Removed]

    private[internal] def removed(): Removed =
      if (_removedRef.get == null)
        val r = Removed(this)
        _removedRef.lazySet(r)
        r
      else _removedRef.get

    def next: Any =
      _next.loop { n =>
        n match
          case d: AtomicOpDescriptor => d.perform(this)
          case _ => return n
      }

    def nextNode: Node = next.unwrap()

    def prevNode: Node =
      val p = correctPrev(null)
      if (p != null) p
      else findPrevNonRemoved(_prev.get)

    def addOneIfEmpty(node: Node): Boolean =
      node._prev.lazySet(this)
      node._next.lazySet(this)
      forever { _ =>
        val n = next
        if (n != this) return false // this is not an empty list!
        if (_next.compareAndSet(this, node)) {
          // added successfully (linearized add) -- fixup the list
          node.finishAdd(this)
          return true
        }
      }

    private[internal] def addNext(node: Node, next: Node): Boolean =
      node._prev.lazySet(this)
      node._next.lazySet(next)
      if (!_next.compareAndSet(next, node)) return false
      // added successfully (linearized add) -- fixup the list
      node.finishAdd(next)
      true

    def addLast(node: Node): Unit =
      while (true) { // lock-free loop on prev.next
        if (prevNode.addNext(node, this)) return
      }

    private[internal] abstract class CondAddOp(
        val newNode: Node
    ) extends AtomicOp[Node]:
      var oldNext: Node | Null = null

      override def complete(affected: Any, failure: Any | Null): Unit =
        val af = affected.asInstanceOf[Node]
        val success = failure == null
        val update = if (success) newNode else oldNext
        if (update != null && af._next.compareAndSet(this, update)) {
          // only the thread the makes this update actually finishes add operation
          if (success) newNode.finishAdd(oldNext)
        }

    def tryCondAddNext(node: Node, next: Node, condAdd: node.CondAddOp): State =
      node._prev.lazySet(this)
      node._next.lazySet(next)
      condAdd.oldNext = next
      if (!_next.compareAndSet(next, condAdd)) return State.Undecided
      // added operation successfully (linearized) -- complete it & fixup the list
      if (condAdd.perform(this) == null) State.Success else State.Failure

    def describeAddLast[T <: Node](node: T): AddLastDesc[T] = AddLastDesc(this, node)

    inline def makeCondAddOp(node: Node, condition: () => Boolean): node.CondAddOp =
      new node.CondAddOp(node) {
        override def prepare(affected: Any): Any | Null =
          if (condition()) null else ConditionFalse
      }

    def addLastIf(node: Node, condition: () => Boolean): Boolean =
      val condAdd = makeCondAddOp(node, condition)
      forever { _ => // lock-free loop on prev.next
        val prev = prevNode // sentinel node is never removed, so prev is always defined
        val state = prev.tryCondAddNext(node, this, condAdd)
        state match
          case State.Success => return true
          case State.Failure => return false
          case _ => ()
      }

    def addLastIfPrev(node: Node, predicate: (Node) => Boolean): Boolean =
      forever { _ => // lock-free loop on prev.next
        val prev = prevNode // sentinel node is never removed, so prev is always defined
        if (!predicate(prev)) return false
        if (prev.addNext(node, this)) return true
      }

    def addLastIfPrevAndIf(
        node: Node,
        predicate: (Node) => Boolean, // prev node predicate
        condition: () => Boolean // atomically checked condition
    ): Boolean =
      val condAdd = makeCondAddOp(node, condition)
      forever { _ => // lock-free loop on prev.next
        val prev = prevNode // sentinel node is never removed, so prev is always defined
        if (!predicate(prev)) return false
        val state = prev.tryCondAddNext(node, this, condAdd)
        state match
          case State.Success => return true
          case State.Failure => return false
          case _ => ()
      }

    private[internal] def finishAdd(next: Node): Unit =
      next._prev.loop { nextPrev =>
        if (this.next != next)
          return // this or next was removed or another node added, remover/adder fixes up links
        if (next._prev.compareAndSet(nextPrev, this)) {
          // This newly added node could have been removed, and the above CAS would have added it physically again.
          // Let us double-check for this situation and correct if needed
          if (isRemoved) next.correctPrev(null)
          return
        }
      }

    @tailrec
    private def findPrevNonRemoved(current: Node): Node =
      if (!current.isRemoved) return current
      findPrevNonRemoved(current._prev.get)

    private inline def forever(inline f: (Unit) => Unit): Nothing =
      while (true) {
        f(())
      }
      throw RuntimeException("impossible reached")

    // @tailrec can't make this tailrec
    private[internal] def correctPrev(op: AtomicOpDescriptor | Null): Node | Null =
      val oldPrev = _prev.get
      var prev: Node = oldPrev
      var last: Node = null // will be set so that last.next === prev
      forever { _ => // move the left until first non-removed node
        val prevNext: Any = prev._next.get

        // fast path to find quickly find prev node when everything is properly linked
        if (prevNext == this) {
          if (oldPrev == prev) return prev // nothing to update -- all is fine, prev found
          // otherwise need to update prev
          if (!this._prev.compareAndSet(oldPrev, prev)) {
            // Note: retry from scratch on failure to update prev
            return correctPrev(op)
          }
          return prev // return the correct prev
        }
        // slow path when we need to help remove operations
        else if (this.isRemoved)
          return null // nothing to do, this node was removed, bail out asap to save time
        else if (prevNext == op)
          return prev // part of the same op -- don't recurse, didn't correct prev
        else if (prevNext.isInstanceOf[AtomicOpDescriptor]) { // help & retry
          val pv = prevNext.asInstanceOf[AtomicOpDescriptor]
          if (op != null && op.isEarlierThan(pv))
            return null // RETRY_ATOMIC
          pv.perform(prev)
          return correctPrev(op) // retry from scratch
        } else if (prevNext.isInstanceOf[Removed]) {
          if (last != null) {
            val pr = prevNext.asInstanceOf[Removed]
            // newly added (prev) node is already removed, correct last.next around it
            if (!last._next.compareAndSet(prev, pr.ref)) {
              return correctPrev(op) // retry from scratch on failure to update next
            }
            prev = last
            last = null
          } else {
            prev = prev._prev.get
          }
        } else { // prevNext is a regular node, but not this -- help delete
          last = prev
          prev = prevNext.asInstanceOf[Node]
        }
      }

    def remove(): Boolean =
      removeOrNext() == null

    // returns null if removed successfully or next node if this node is already removed
    private[internal] def removeOrNext(): Node | Null =
      forever { _ => // lock-free loop on next
        val next = this.next
        if (next.isInstanceOf[Removed])
          return next
            .asInstanceOf[Removed]
            .ref // was already removed -- don't try to help (original thread will take care)
        if (next == this) return next.asInstanceOf[Node] // was not even added
        val removed = next.asInstanceOf[Node].removed()
        if (_next.compareAndSet(next, removed)) {
          // was removed successfully (linearized remove) -- fixup the list
          next.asInstanceOf[Node].correctPrev(null)
          return null
        }
      }

    // Helps with removal of this node
    def helpRemove(): Node | Null =
      // Note: this node must be already removed
      next.asInstanceOf[Removed].ref.helpRemovePrev()

    // Helps with removal of nodes that are previous to this
    private[internal] def helpRemovePrev(): Node | Null =
      // We need to call correctPrev on a non-removed node to ensure progress, since correctPrev bails out when
      // called on a removed node. There's always at least one non-removed node (list head).
      var node = this
      forever { _ =>
        val next = node.next
        if (!next.isInstanceOf[Removed]) break
        node = next.asInstanceOf[Removed].ref
      }
      // Found a non-removed node
      node.correctPrev(null)

    def removeFirstOrNull(): Node | Null =
      forever { _ => // try to linearize
        val first = next.asInstanceOf[Node]
        if (first == this) return null
        if (first.remove()) return first
        first.helpRemove() // must help remove to ensure lock-freedom
      }
