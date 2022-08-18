package sttp
package fx

import _root_.fx.{given, *}
import munit.fx.ScalaFXSuite
import sttp.client3.NoBody
import sttp.client3.ByteArrayBody

import java.nio.ByteBuffer
import java.util.concurrent.Flow
import java.nio.charset.Charset
import sttp.client3.ByteBufferBody
import java.io.ByteArrayInputStream
import sttp.client3.InputStreamBody

trait ToHttpBodyMapperFixtures { self: ScalaFXSuite =>

  val noBody = FunFixture(setup = _ => NoBody, teardown = _ => ())

  val byteArrayBody =
    FunFixture(setup = _ => ByteArrayBody("test".getBytes()), teardown = _ => ())

  val byteBufferBody = FunFixture(
    setup = _ => ByteBufferBody(ByteBuffer.wrap("test".getBytes())),
    teardown = _ => ())

  val inputStreamBody = FunFixture(
    setup = _ => InputStreamBody(new ByteArrayInputStream("test".getBytes)),
    teardown = _ => ())

}
