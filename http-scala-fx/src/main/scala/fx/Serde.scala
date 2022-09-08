package fx

import scala.deriving.Mirror
import scala.compiletime.erasedValue

import java.nio.ByteBuffer

/**
 * Represents the ability to serialize / deserialize a list of byte buffers to some type A.
 * Serialization must round-trip. That is:
 *
 * {{{
 * Serde[A].deserialize(Serde[A].serialize(a)) == a
 * }}}
 */
trait Serde[A]:
  /**
   * Deserializing may fail, for many different reasons. The non-empty string allows you to
   * report them all at one time.
   */
  def deserialize(a: List[ByteBuffer]): ::[String] | A

  /**
   * Serializes the A to a list of bytebuffers.
   */
  def serialize(a: A): List[ByteBuffer]

object Serde:
  def apply[A: Serde](): Serde[A] = summon[Serde[A]]
