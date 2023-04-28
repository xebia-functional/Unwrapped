package sttp
package unwrapped

import _root_.unwrapped.{given, *}
import munit.unwrapped.UnwrappedSuite
import sttp.client3.NoBody
import sttp.client3.ByteArrayBody
import sttp.client3.StringBody

import java.nio.ByteBuffer
import java.util.concurrent.Flow
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import sttp.client3.ByteBufferBody
import java.io.ByteArrayInputStream
import sttp.client3.InputStreamBody
import java.nio.file.Files
import sttp.client3.FileBody
import sttp.client3.internal.SttpFile
import sttp.client3.StreamBody
import sttp.client3.MultipartBody
import sttp.capabilities.Effect

trait ToHttpBodyMapperFixtures { self: UnwrappedSuite =>

  val noBody = FunFixture(setup = _ => NoBody, teardown = _ => ())

  val byteArrayBody =
    FunFixture(setup = _ => ByteArrayBody("test".getBytes()), teardown = _ => ())

  val byteBufferBody = FunFixture(
    setup = _ => ByteBufferBody(ByteBuffer.wrap("test".getBytes())),
    teardown = _ => ())

  val inputStreamBody = FunFixture(
    setup = _ => InputStreamBody(new ByteArrayInputStream("test".getBytes)),
    teardown = _ => ())

  val fileBody = FunFixture(
    setup = _ => {
      Resource.apply(
        {
          val file = Files.createTempFile("fileBodyTest", ".txt")
          Files.write(file, "test".getBytes())
          FileBody(SttpFile.fromPath(file))
        },
        (f: FileBody, exitCase: ExitCase) => {
          Files.deleteIfExists(f.f.toPath)
        }
      )
    },
    teardown = _ => ()
  )

  val streamBody = FunFixture(
    setup = _ => StreamBody(new ReceiveStreams {})(streamOf("test".getBytes().toList: _*)),
    teardown = _ => ())

  val multiparts = FunFixture.map3(
    noBody,
    byteArrayBody,
    FunFixture.map3(byteBufferBody, inputStreamBody, FunFixture.map2(fileBody, streamBody)))

  val stringBody =
    FunFixture(setup = _ => StringBody("test", StandardCharsets.UTF_8.name), teardown = _ => ())

}
