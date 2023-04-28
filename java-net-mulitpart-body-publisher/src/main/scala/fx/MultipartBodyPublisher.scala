/**
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International
 * License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 * From https://stackoverflow.com/a/54675316/708929 Accessed on 2022/08/07T16:05:00.000Z-5:00
 * Authors: https://stackoverflow.com/users/6558116/ittupelo
 * https://stackoverflow.com/users/1746118/naman Converted to scala by
 * https://stackoverflow.com/users/708929/jack-viers
 */
package unwrapped

import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Supplier
import scala.jdk.CollectionConverters.*

/**
 * An immutable multi-part body publisher. Given a boundary, and a non empty queue of part
 * specifications added with addPart, this will publish multipart body requests via http.
 *
 * {{{
 * val publisher = MultiPartBodyPublisher(scala.util.Random(new SecureRandom()))
 * .addPart("myField", "myFieldValue")
 * .addPart("myFile", "/Users/alice/Documents/someFile.txt")
 * .addPart(
 * "secondFile",
 * () => readFile(myPath),
 * myPath.toFile().getName(),
 * myPath,
 * "application/octet-stream"
 * )
 * .bodyPublisher()
 * }}}
 */
class MultiPartBodyPublisher private (
    // immutable queues have constant time append, so this helps
    private val parts: scala.collection.immutable.Queue[PartSpecification],
    val boundary: Boundary) {

  /**
   * Adds a string body part to the multipart body.
   */
  def addPart(name: String, value: String): MultiPartBodyPublisher = new MultiPartBodyPublisher(
    parts.appended(
      PartSpecification(PartSpecificationName(name), PartSpecificationValue(value), boundary)),
    boundary)

  /**
   * Adds a path body part to the multipart body. Throws when the file at the path is
   * inaccessible.
   */
  def addPart(name: String, value: Path): MultiPartBodyPublisher = new MultiPartBodyPublisher(
    parts.appended(PartSpecification(PartSpecificationName(name), value, boundary)),
    boundary)

  /**
   * Adds a file body part to the multipart body. Throws when the file at the path is
   * inaccessible.
   */
  def addPart(
      name: String,
      value: () => InputStream,
      filename: String,
      path: Path,
      contentType: String): MultiPartBodyPublisher = new MultiPartBodyPublisher(
    parts.appended(
      PartSpecification(
        PartSpecificationName(name),
        PartSpecificationFilename(filename),
        path,
        PartSpecificationInputStream(value),
        PartSpecificationContentType(contentType),
        boundary
      )),
    boundary)

  /**
   * Adds a generic input stream to the multi-part body
   */
  def addPart(
      name: String,
      value: () => InputStream,
      contentType: String): MultiPartBodyPublisher = new MultiPartBodyPublisher(
    parts.appended(
      PartSpecification(
        PartSpecificationName(name),
        PartSpecificationInputStream(value),
        PartSpecificationContentType(contentType),
        boundary
      )
    ),
    boundary
  )

  /**
   * Builds a body publisher from the bytes of all the added parts. Throws when there are no
   * body parts to send. Throws when parts is empty or cannot be encoded.
   */
  def unsafeTobodyPublisher(): BodyPublisher =
    if (parts.nonEmpty)
      BodyPublishers.ofByteArrays(
        parts
          .appended(PartSpecification(boundary))
          .iterator
          .map(PartSpecification.toPartSpec(_))
          .toList
          .asJava)
    else throw IllegalStateException("parts must be non-empty")
}

object MultiPartBodyPublisher:
  /**
   * Creates an empty publisher when given a string boundary.
   */
  def apply(boundary: Boundary): MultiPartBodyPublisher =
    new MultiPartBodyPublisher(scala.collection.immutable.Queue.empty, boundary)

  /**
   * Creates an empty publisher when given a random from which to produce a boundary.
   */
  def apply(random: scala.util.Random): MultiPartBodyPublisher =
    new MultiPartBodyPublisher(
      scala.collection.immutable.Queue.empty,
      Boundary(random.nextLong().toHexString))
