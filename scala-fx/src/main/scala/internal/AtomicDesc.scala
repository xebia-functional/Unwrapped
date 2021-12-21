package fx.internal

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.annotation.targetName
import scala.util.control.Breaks._
import fx.internal.LockFreeLinkedList.Node

abstract class AtomicDesc {
  var atomicOp: AtomicOp[
    ?
  ] = null // the reference to parent atomicOp, init when AtomicOp is created
  def prepare(op: AtomicOp[?]): Any | Null // returns `null` if prepared successfully
  def complete(op: AtomicOp[?], failure: Any | Null): Unit // decision == null if success
}