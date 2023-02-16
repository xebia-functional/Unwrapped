package munit

import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.syntax.given
import io.{circe => ic}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source
import scala.util.Try

/**
 * Provides snapshot testing capabilities for munit.
 *
 * Snapshot tests are great ways of preventing regressions in the outputs of complex processes.
 * An example is compiler stage tree and context information. Another example is a complex web
 * page rendering, or indentation engine output.
 *
 * Given a json encodeable output, the first time the test is run, the output is encoded and
 * recorded to a file in a directory called "snapshots" in the same directory as the test suite
 * containing the snapshot test, using the filename of the test suite, the name of the test, and
 * the line number of the test.
 *
 * In subsequent runs, the result is compared to the decoded result recorded in the file.
 * Snapshot files should be stored with the test source code in version control.
 *
 * To clear an outcome of a previous run, attach the tag `clear` to the test name:
 * `"testName".clear` or add it to the TestOptions passed to `snapshotTest` or
 * `FunFixture#snapshotTest` using `.tag(Clear)`.
 *
 * Test locations can be overridden on a suite by suite basis using `snapshotDirectoryName`.
 */
abstract class SnapshotSuite extends FunSuite:

  /**
   * Provides the name of the snapshot file storage directory. Defalut is "snapshots".
   *
   * @return
   *   The name of the snapshot directory relative to this test suite.
   */
  protected def snapshotDirectoryName: String = "snapshots"

  override def munitTestTransforms: List[TestTransform] =
    clearTransform :: super.munitTestTransforms

  /**
   * An munit Tag indicating the previous snapshot file should be cleared before the test is
   * run. Cleared tests will always pass if no errors are thrown during the running of the test
   * body.
   */
  val Clear = Tag("clear")

  /**
   * A derived given circe Codec[A].
   *
   * @return
   *   Given an Encoder[A] and a Decoder[A], a Codec[A] instance using both.
   */
  given codec[A: Encoder: Decoder]: Codec[A] =
    ic.Codec.from(summon, summon)

  /**
   * Given a json codec for the type of the result of the snapshot test body, execute a snapshot
   * test on the body results.
   *
   * The first time the test is run, the json encoded result is recorded to a file in the same
   * directory as the test file using the location, test name, and line number of the snapshot
   * test. On subsequent runs, the json file is decoded and compared to the results. If they are
   * not equal, a diff is dislayed.
   *
   * @param name
   * @param body
   */
  def snapshotTest[A: ic.Codec](name: String)(body: => A)(using Location): Unit =
    snapshotTest(TestOptions(name))(body)

  private final def createSnapshotDirectoryIfItDoesntExist()(using location: Location): Unit =
    val directoryPath = Paths.get(
      location.path.replaceAllLiterally(location.filename, ""),
      snapshotDirectoryName
    )
    val directory = directoryPath.toFile()
    if (directory.exists() && !directory.isDirectory())
      directory.delete()
    else if (!directory.exists())
      Files.createDirectory(directoryPath)
    else ()

  /**
   * Given a json codec for the type of the result of the snapshot test body, execute a snapshot
   * test on the body results.
   *
   * The first time the test is run, the json encoded result is recorded to a file in the same
   * directory as the test file using the location, test name, and line number of the snapshot
   * test. On subsequent runs, the json file is decoded and compared to the results. If they are
   * not equal, a diff is dislayed.
   *
   * @param options
   * @param body
   * @param l
   */
  def snapshotTest[A: ic.Codec](options: TestOptions)(body: => A)(
      using location: Location): Unit =
    test(options) {
      val result = body
      createSnapshotDirectoryIfItDoesntExist()
      val file = Paths
        .get(
          location.path.replaceAllLiterally(location.filename, ""),
          snapshotDirectoryName,
          s"${location.filename}_${URLEncoder.encode(options.name)}_${location.line}.json"
        )
        .toFile()
      if (file.exists())
        val expectedSource = Source.fromFile(file)
        val sourceString = expectedSource.mkString
        val _ = Try(expectedSource.close())
        val testResult = decode[A](sourceString).map(assertEquals(result, _)).toTry.get
      else Files.writeString(file.toPath(), result.asJson.noSpaces)
    }

  /**
   * When a test's tags set contains a `Clear` tag, this transform will delete the snapshot file
   * associated with the test from the `snapshotDirectoryName` directory relative to this test
   * suite.
   *
   * @return
   *   A `TestTransform` that will delete the snapshot associated with the test.
   */
  final def clearTransform: TestTransform =
    new TestTransform(
      Clear.value,
      { (t: Test) =>
        if (t.tags(Clear))
          t.withBodyMap { (value: TestValue) =>
            val location = t.location
            Try(
              Paths
                .get(
                  location.path.replaceAllLiterally(location.filename, ""),
                  snapshotDirectoryName,
                  s"${location.filename}_${URLEncoder.encode(t.name)}_${location.line}.json"
                )
                .toFile()
                .delete()
            )
            value
          }
        else
          t
      }
    )

  /**
   * Adds a `Clear` tag to the given test options
   *
   * @return
   *   A new TestOptions containing a `Clear` test tag in its `tags` set
   */
  extension (options: TestOptions) def clear: TestOptions = options.tag(Clear)

  extension [A](fixture: FunFixture[A])
    /**
     * Provides snapshot testing capabilities using Fixtures.
     *
     * Given a json codec for the type of the result of the snapshot test body, execute a
     * snapshot test on the body results.
     *
     * The first time the test is run, the json encoded result is recorded to a file in the same
     * directory as the test file using the location, test name, and line number of the snapshot
     * test. On subsequent runs, the json file is decoded and compared to the results. If they
     * are not equal, a diff is dislayed.
     *
     * @param B
     * @param name
     * @param body
     */
    def snapshotTest[B: Codec](name: String)(body: A => B)(using Location): Unit =
      snapshotTest(TestOptions(name))(body)

    /**
     * Provides snapshot testing capabilities using Fixtures.
     *
     * Given a json codec for the type of the result of the snapshot test body, execute a
     * snapshot test on the body results.
     *
     * The first time the test is run, the json encoded result is recorded to a file in the same
     * directory as the test file using the location, test name, and line number of the snapshot
     * test. On subsequent runs, the json file is decoded and compared to the results. If they
     * are not equal, a diff is dislayed.
     *
     * @param B
     * @param options
     * @param body
     * @param l
     */
    def snapshotTest[B: Codec](options: TestOptions)(body: A => B)(
        using location: Location): Unit =
      fixture.test(options) { fixture =>
        val result = body(fixture)
        createSnapshotDirectoryIfItDoesntExist()
        val file = Paths
          .get(
            location.path.replaceAllLiterally(location.filename, ""),
            snapshotDirectoryName,
            s"${location.filename}_${URLEncoder.encode(options.name)}_${location.line}.json"
          )
          .toFile()
        if (file.exists())
          val expectedSource = Source.fromFile(file)
          val sourceString = expectedSource.mkString
          val _ = Try(expectedSource.close())
          val testResult = decode[B](sourceString).map(assertEquals(result, _)).toTry.get
        else Files.writeString(file.toPath(), result.asJson.noSpaces)
      }
