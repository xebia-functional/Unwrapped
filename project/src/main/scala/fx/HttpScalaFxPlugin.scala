package fx

import sbt._
import Keys._
import com.fasterxml.jackson.dataformat.csv.{CsvMapper, CsvParser, CsvSchema}
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object HttpScalaFxPlugin extends AutoPlugin {

  object autoImport {
    lazy val generateMediaTypes = taskKey[Seq[File]]("Generate fx/MediaTypes.scala")
    lazy val mediaTypeCsvDir = settingKey[File](
      "The source directory containing the IANA media types. Default is baseDirectory.value/src/main/mediaTypes.")
    lazy val targetPackage = settingKey[String](
      "the target package for the MediaTypes object generated. Defaults to 'fx'")
    lazy val generateMediaTypeSettings = Seq(
      Compile / targetPackage := "fx",
      Compile / generateMediaTypes := {
        println("generating MediaTypes.scala")
        val schema: CsvSchema =
          CsvSchema.builder().setUseHeader(true).build()
        val mapper: CsvMapper =
          new CsvMapper()
        val outputDirectory = (Compile / sourceManaged).value / (Compile / targetPackage).value
        val outputFile = outputDirectory / "MediaTypes.scala"
        val newline = IO.Newline
        val indent = "  "
        val sb = new StringBuilder("")
        try { IO.delete(outputFile) }
        catch { case _: Throwable => () }
        try { IO.createDirectory(outputDirectory) }
        catch { case _: Throwable => () }

        val lb = ListBuffer("string")

        sb.append(s"package fx$newline$newline")
        sb.append(s"object MediaTypes:$newline")

        val entryFiles = IO.listFiles(mediaTypeCsvDir.value)

        IO.listFiles(mediaTypeCsvDir.value)
          .map { file: File =>
            print(".")
            val fileAsString = IO.read(file)
            val values = mapper
              .readerForMapOf(classOf[String])
              .`with`(schema)
              .readValues[java.util.Map[String, String]](fileAsString)
              .readAll()
              .asScala
              .toList
              .map(_.asScala.toMap)
            val objectType = s"${file.getName().replaceAllLiterally(".csv", "")}"
            sb.append(s"""${indent}object $objectType:$newline""")
            values.map { value =>
              for {
                name <- value.get("Name")
                template <- value.get("Template")
                mediaType =
                  if (template == "") s"""MediaType("$name")"""
                  else s"""MediaType("$template")"""
              } {
                lb.append(s"$objectType.`$name`")
                sb.append(
                  s"""$indent${indent}val `$name` = $newline$indent$indent$indent$mediaType$newline"""
                )
              }
            }
            sb.append(s"$newline")
          }
          .toList

        sb.append(
          s"$newline${indent}val mediaTypes: Set[MediaType] = $newline$indent${indent}Set(")

        for {
          reference <- lb
          if reference != "string"
        } sb.append(s"""$newline$indent$indent$indent$reference,""")

        sb.append(s"$newline$indent$indent)")

        val result = if (entryFiles.nonEmpty) {
          IO.append(outputFile, sb.toString())
          Seq(outputFile)
        } else Seq.empty
        print("finished generating MediaTypes.scala\n")
        result
      },
      mediaTypeCsvDir := baseDirectory.value / "src" / "main" / "mediaTypes",
      Compile / sourceGenerators += Compile / generateMediaTypes
    )

  }

  import autoImport._

}
