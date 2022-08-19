package fx

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import scala.util.Try

sealed trait PartSpecification

object PartSpecification:
  private def fold[B](partSpec: PartSpecification)(
      whenStringPartSpec: (PartSpecification.StringPartSpec) => B,
      whenFilePartSpec: (PartSpecification.FilePartSpec) => B,
      whenPathPartSpec: (PartSpecification.PathPartSpec) => B,
      whenStreamPartSpec: (PartSpecification.StreamPartSpec) => B,
      whenFinalBoundaryPartSpec: (PartSpecification.FinalBoundaryPartSpec) => B): B =
    partSpec match
      case x: PartSpecification.StringPartSpec => whenStringPartSpec(x)
      case x: PartSpecification.PathPartSpec => whenPathPartSpec(x)
      case x: PartSpecification.FilePartSpec => whenFilePartSpec(x)
      case x: PartSpecification.StreamPartSpec => whenStreamPartSpec(x)
      case x: PartSpecification.FinalBoundaryPartSpec => whenFinalBoundaryPartSpec(x)

  private case class StringPartSpec(
      name: PartSpecificationName,
      value: PartSpecificationValue,
      boundary: Boundary)
      extends PartSpecification

  private case class PathPartSpec(name: PartSpecificationName, value: Path, boundary: Boundary)
      extends PartSpecification

  private case class FilePartSpec(
      name: PartSpecificationName,
      filename: PartSpecificationFilename,
      path: Path,
      value: PartSpecificationInputStream,
      contentType: PartSpecificationContentType,
      boundary: Boundary)
      extends PartSpecification

  private case class StreamPartSpec(
      name: PartSpecificationName,
      value: PartSpecificationInputStream,
      contentType: PartSpecificationContentType,
      boundary: Boundary
  ) extends PartSpecification

  private case class FinalBoundaryPartSpec(val boundary: Boundary) extends PartSpecification

  def apply(
      name: PartSpecificationName,
      value: PartSpecificationValue,
      boundary: Boundary): PartSpecification =
    StringPartSpec(name, value, boundary)

  def apply(name: PartSpecificationName, value: Path, boundary: Boundary): PartSpecification =
    PathPartSpec(name, value, boundary)

  def apply(
      name: PartSpecificationName,
      filename: PartSpecificationFilename,
      path: Path,
      value: PartSpecificationInputStream,
      contentType: PartSpecificationContentType,
      boundary: Boundary
  ): PartSpecification =
    FilePartSpec(name, filename, path, value, contentType, boundary)

  def apply(
      name: PartSpecificationName,
      value: PartSpecificationInputStream,
      contentType: PartSpecificationContentType,
      boundary: Boundary): PartSpecification =
    StreamPartSpec(name, value, contentType, boundary)

  def apply(boundary: Boundary): PartSpecification =
    FinalBoundaryPartSpec(boundary)

  private given ToPartSpec[StringPartSpec] with
    extension (p: StringPartSpec)
      def toPartSpec: Array[Byte] =
        s"""|--${p.boundary.value}
            |Content-Disposition: form-data; name=${p.name.value}
            |Content-Type: text/plain; charset=UTF-8
            |
            |${p
          .value
          .value}
            |""".stripMargin.replace("/n", "/r/n").getBytes(Charset.forName("UTF-8"))

  private given ToPartSpec[FilePartSpec] with
    extension (p: FilePartSpec)
      def toPartSpec: Array[Byte] =
        val path = p.path
        s"""|--${p.boundary}
            |Content-Disposition: form-data; name=${p.name.value}; filename=${p.filename.value}
            |Content-Type: ${Try { Option(Files.probeContentType(path)).get }.fold(
          _ => "application/octet-stream",
          identity)}
            |""".stripMargin.getBytes() ++ Files.newInputStream(path).readAllBytes() ++ "\r\n".getBytes

  private given ToPartSpec[PathPartSpec] with
    extension (p: PathPartSpec)
      def toPartSpec: Array[Byte] =
        val path: Path = p.value
        s"""|--${p.boundary.value}
            |Content-Disposition: form-data; name=${p
          .name
          .value}; filename=${path.toFile().getName()}
            |Content-Type: ${Try { Option(Files.probeContentType(path)).get }.fold(
          _ => "application/octet-stream",
          identity)}""".stripMargin.getBytes() ++ Files.newInputStream(path).readAllBytes() ++ "\r\n".getBytes

  private given ToPartSpec[StreamPartSpec] with
    extension (p: StreamPartSpec)
      def toPartSpec: Array[Byte] =
        s"""|--${p.boundary.value}
            |Content-Disposition: form-data; name="${p.name.value}; filename=${p.name.value}
            |Content-Type: ${p.contentType.value}
            |
            |""".stripMargin.replace("\n", "\r\n").getBytes(Charset.forName("UTF-8")) ++ p
          .value
          .value()
          .readAllBytes() ++ "\r\n".getBytes

  private given ToPartSpec[FinalBoundaryPartSpec] with
    extension (p: FinalBoundaryPartSpec)
      def toPartSpec: Array[Byte] =
        s"""|--${p.boundary.value}--""".stripMargin.getBytes(Charset.forName("UTF-8"))

  def toPartSpec(p: PartSpecification): Array[Byte] =
    fold(p)(
      _.toPartSpec,
      _.toPartSpec,
      _.toPartSpec,
      _.toPartSpec,
      _.toPartSpec
    )
