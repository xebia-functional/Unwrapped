package fx

import sbt._
import sbt.util.CacheImplicits._
import sbt.Keys._

import xsbti.compile.{CompileOrder => CO, _}
import scala.sys.process._
import scala.collection.JavaConverters._

object ForceableCompilationPlugin extends AutoPlugin {
  object autoImport {
    lazy val forceCompilation =
      settingKey[Boolean]("Forces a full compilation. Default is false.")
    lazy val isx8664 = taskKey[Boolean]("Checks to see if the processor is x86-64.")
    lazy val installCoursier = taskKey[Unit]("Install coursier if needed")
    lazy val hasCoursier = taskKey[Boolean]("Checks to see if coursier is installed")
    lazy val hasHomebrew = taskKey[Boolean]("Checks to see if homebrew is installed")

  }

  private def which(programName: String): Int = s"which $programName".!

  private def hc(): Boolean = isZero(which("cs"))

  private def hb(): Boolean = isZero(which("brew"))

  private def isZero(actualStatus: Int): Boolean = actualStatus == 0

  private def _isX8664(): Boolean = "uname -a".lineStream.mkString.contains("x86_64")

  private def installCoursierX86() = {
    if (!isZero(
        ("curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" #| "gzip -d" #> "cs" #&& "chmod +x cs" #&& "./cs setup").!)) {
      throw new IllegalStateException(
        "Installing coursier failed. Please run `curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup` to install coursier.")
    } else ()
  }
  private def installCoursierArm() = {
    if (!isZero(
        "curl -fL https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz" #| "gzip -d" #> "cs" #&& "chmod +x cs" #&& "./cs setup" !)) {
      throw new IllegalStateException(
        "Installing coursier failed. Please run `curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup` to install coursier.")
    } else ()
  }
  private def installCoursierWithHomebrew() =
    if (!isZero("brew install coursier/formulas/coursier" #&& "cs setup" !)) {
      throw new IllegalStateException(
        "Installing coursier failed. If you have an M1 or M2, please run `curl -fL https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-apple-darwin.gz | gzip -d > cs && chmod +x cs && (xattr -d com.apple.quarantine cs || true) && ./cs setup` to install coursier. Otherwise run `brew install coursier/formulas/coursier && cs setup`.")
    } else ()

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    hasCoursier := hasCoursier.previous.getOrElse(hc()),
    hasHomebrew := hasHomebrew.previous.getOrElse(hb()),
    isx8664 := isx8664.previous.getOrElse(_isX8664()),
    installCoursier := {
      val hcv = hasCoursier.value
      val hhbv = hasHomebrew.value
      val ix8664v = isx8664.value
      if (!hcv && hhbv) {
        installCoursierWithHomebrew()
      } else if (!hcv && ix8664v) {
        installCoursierX86()
      } else if (!hcv && !ix8664v) {
        installCoursierArm()
      } else ()
    },
    Test / forceCompilation := false,
    Compile / forceCompilation := false,
    Compile / compileIncremental := {
      if (forceCompilation.value) {

        val log = streams.value.log

        val _ = installCoursier.value

        val classesDir = (Compile / classDirectory).value.getAbsolutePath

        sbt.io.IO.createDirectories(Seq(new java.io.File(classesDir)))

        val compileSources = (Compile / sources).value.map(_.getAbsolutePath)

        // format: off
        val compileCommand =
          s"cs launch scalac:${(Compile / scalaVersion).value} -- ${(Compile / scalacOptions).value.mkString(" ")} -classpath ${(Compile / dependencyClasspath).value.map(_.data.getAbsolutePath).mkString(":")} -d ${(Compile / classDirectory).value.getAbsolutePath} ${compileSources.mkString(" ")}"
        // format: on

        val compilerMessages = compileCommand.lineStream.mkString

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

        val _ = installCoursier.value

        val classesDir = (Test / classDirectory).value.getAbsolutePath

        sbt.io.IO.createDirectories(Seq(new java.io.File(classesDir)))

        log.info(s"Test scalacOptions: ${(Test / scalacOptions).value}")

        val compileSources = (Test / sources).value.map(_.getAbsolutePath)

        // format: off
        val compileCommand =
          s"cs launch scalac:${(Test / scalaVersion).value} -- ${(Test / scalacOptions).value.mkString(" ")} -classpath ${(Test / dependencyClasspath).value.map(_.data.getAbsolutePath).mkString(":")} -d ${(Test / classDirectory).value.getAbsolutePath} ${compileSources.mkString(" ")}"
        // format: on
        log.info(s"compile command: $compileCommand")

        val compilerMessages = compileCommand.lineStream.mkString

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
