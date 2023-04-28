package sttp
package unwrapped

import sttp.capabilities.Streams
import _root_.unwrapped.Receive
import _root_.unwrapped.Send

/**
 * Models the streaming capability of TBD.
 */
trait ReceiveStreams extends Streams[ReceiveStreams]:
  override type BinaryStream = Receive[Byte]

  /**
   * Pipe[A, B] is Receive[A]#transform in TBD.
   */
  override type Pipe[A, B] = Send[B] ?=> (A => Unit) => Receive[B]
