package fx

import sbt._
import Keys._

import xsbti.compile.{CompileOrder => CO, _}
import scala.sys.process
import scala.collection.JavaConverters._

object ForceableCompilationPlugin extends AutoPlugin {
  object autoImport {
    lazy val forceCompilation =
      settingKey[Boolean]("Forces a full compilation. Default is false.")

  }
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    Test / forceCompilation := false,
    Compile / forceCompilation := false,
    Compile / compileIncremental := {
      if (forceCompilation.value) {

        val log = streams.value.log
        val classesDir = (Compile / classDirectory).value.getAbsolutePath

        sbt.io.IO.createDirectories(Seq(new java.io.File(classesDir)))

        log.info(s"Compile scalacOptions: ${(Compile / scalacOptions).value}")

        log.info(s"compile command: ${Seq(
          "scalac",
          (Compile / scalacOptions).value.mkString(" "),
          "-classpath",
          (Compile / dependencyClasspath).value.map(_.data.getAbsolutePath).mkString(":"),
          "-d",
          (Compile / classDirectory).value.getAbsolutePath,
          (Compile / sources).value.map(_.getAbsolutePath).mkString(" ")
        ).mkString(" ")}")

        val compilerMessages = process
          .Process(
            Seq(
              "scalac",
              (Compile / scalacOptions).value.mkString(" "),
              "-classpath",
              (Compile / dependencyClasspath).value.map(_.data.getAbsolutePath).mkString(":"),
              "-d",
              (Compile / classDirectory).value.getAbsolutePath,
              (Compile / sources).value.map(_.getAbsolutePath).mkString(" ")
            ).mkString(" "))
          .lineStream
          .mkString

        log.info(compilerMessages)
        xsbti
          .compile
          .CompileResult
          .of(
            sbt.internal.inc.Analysis.empty,
            xsbti
              .compile
              .MiniSetup
              .of(
                new MultipleOutput {
                  override def getOutputGroups: Array[OutputGroup] = Array.empty[OutputGroup]
                },
                xsbti
                  .compile
                  .MiniOptions
                  .of(
                    Array.empty[FileHash],
                    (Compile / scalacOptions).value.toArray,
                    (compile / javacOptions).value.toArray),
                (Compile / scalaVersion).value,
                CompileOrder.Mixed,
                false,
                Array.empty[xsbti.T2[String, String]]
              ),
            true
          )
      } else (Compile / compileIncremental).value
    },
    Test / compileIncremental := {
      if (forceCompilation.value) {

        val log = streams.value.log
        val classesDir = (Test / classDirectory).value.getAbsolutePath

        sbt.io.IO.createDirectories(Seq(new java.io.File(classesDir)))

        log.info(s"Test scalacOptions: ${(Test / scalacOptions).value}")

        log.info(s"compile command: ${Seq(
          "scalac",
          (Test / scalacOptions).value.mkString(" "),
          "-classpath",
          (Test / dependencyClasspath).value.map(_.data.getAbsolutePath).mkString(":"),
          "-d",
          (Test / classDirectory).value.getAbsolutePath,
          (Test / sources).value.map(_.getAbsolutePath).mkString(" ")
        ).mkString(" ")}")

        val compilerMessages = process
          .Process(
            Seq(
              "scalac",
              (Test / scalacOptions).value.mkString(" "),
              "-classpath",
              (Test / dependencyClasspath).value.map(_.data.getAbsolutePath).mkString(":"),
              "-d",
              (Test / classDirectory).value.getAbsolutePath,
              (Test / sources).value.map(_.getAbsolutePath).mkString(" ")
            ).mkString(" "))
          .lineStream
          .mkString

        log.info(compilerMessages)
        xsbti
          .compile
          .CompileResult
          .of(
            sbt.internal.inc.Analysis.empty,
            xsbti
              .compile
              .MiniSetup
              .of(
                new MultipleOutput {
                  override def getOutputGroups: Array[OutputGroup] = Array.empty[OutputGroup]
                },
                xsbti
                  .compile
                  .MiniOptions
                  .of(
                    Array.empty[FileHash],
                    (Test / scalacOptions).value.toArray,
                    (compile / javacOptions).value.toArray),
                (Test / scalaVersion).value,
                CompileOrder.Mixed,
                false,
                Array.empty[xsbti.T2[String, String]]
              ),
            true
          )
      } else (Test / compileIncremental).value
    },
    Test / previousCompile := {
      if (forceCompilation.value) {
        xsbti
          .compile
          .PreviousResult
          .of(
            java.util.Optional.empty[xsbti.compile.CompileAnalysis](),
            java.util.Optional.empty[xsbti.compile.MiniSetup]())
      } else (Test / previousCompile).value
    },
    Compile / previousCompile := {
      if (forceCompilation.value) {
        xsbti
          .compile
          .PreviousResult
          .of(
            java.util.Optional.empty[xsbti.compile.CompileAnalysis](),
            java.util.Optional.empty[xsbti.compile.MiniSetup]())
      } else (Compile / previousCompile).value
    }
  )
}
