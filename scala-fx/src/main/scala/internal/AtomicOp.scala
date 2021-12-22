package fx.internal

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.targetName

private[fx] abstract class AtomicOpDescriptor:
  /**
   * Returns `null` is operation was performed successfully or some other object that indicates
   * the failure reason.
   */
  def perform(affected: Any): Any | Null

  /**
   * Returns reference to atomic operation that this descriptor is a part of or `null` if not a
   * part of any [AtomicOp].
   */
  var atomicOp: AtomicOp[?] = null

  def isEarlierThan(that: AtomicOpDescriptor): Boolean =
    if (atomicOp == null) return false
    if (that.atomicOp == null) return false
    atomicOp.opSequence < that.atomicOp.opSequence

class Symbol(val value: String) extends AnyVal

private[internal] val Undecided: Symbol = Symbol("Undecided")
private[internal] val RetryAtomic: Symbol = Symbol("RetryAtomic")
private[internal] val RemovePrepared: Symbol = Symbol("RemovePrepared")
private[internal] val ConditionFalse: Symbol = Symbol("ConditionFalse")
private[internal] val ListEmpty: Symbol = Symbol("ListEmpty")

private[fx] abstract class AtomicOp[-T] extends AtomicOpDescriptor:
  private val _consensus: AtomicReference[Any] = AtomicReference(Undecided)

  // // Returns NO_DECISION when there is not decision yet
  def consensus: Any = _consensus.get

  def isDecided: Boolean = _consensus.get != Undecided

  val opSequence: Long = 0L

  atomicOp = this

  def decide(decision: Any): Any =
    assert { decision != Undecided }
    val current = _consensus.get
    if (current != Undecided) return current
    if (_consensus.compareAndSet(Undecided, decision)) return decision
    return _consensus.get

  def prepare(affected: Any): Any | Null // `null` if Ok, or failure reason

  def complete(
      affected: Any,
      failure: Any | Null): Unit // failure != null if failed to prepare op

  def perform(affected: Any): Any =
    // make decision on status
    var decision = this._consensus.get
    if (decision == Undecided) {
      decision = decide(prepare(affected))
    }
    // complete operation
    complete(affected, decision)
    decision
