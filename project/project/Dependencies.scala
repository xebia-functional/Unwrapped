import sbt._

object Dependencies {

  // define versions, The variable name must be camel case by module name
  object Versions {
    val junit = "4.13.2"
    val sbtJmh = "0.4.3"
    val munit = "0.7.29"
    val jmhCore = "1.35"
    val sbtMdoc = "2.3.2"
    val sbtGithub = "0.11.2"
    val sbtScalafmt = "2.4.6"
    val scalacheck = "1.16.0"
    val sbtCiRelease = "1.5.10"
    val sbtGithubMdoc = "0.11.2"
    val junitInterface = "0.7.29"
    val munitScalacheck = "0.7.29"
    val jmhGeneratorBytecode = "1.35"
    val sbtDependencyUpdates = "1.2.1"
    val jmhGeneratorReflection = "1.35"
    val sbtExplicitDependencies = "0.2.16"
  }

  object Compile {
    // in compile because munit-scala-fx depends on munit directly
    val munitScalacheck = "org.scalameta" %% "munit-scalacheck" % Versions.munitScalacheck
    // munit transitive dependency conflict
    val junit = "junit" % "junit" % Versions.junit
    val munit = "org.scalameta" %% "munit" % Versions.munit
    val junitInterface = "org.scalameta" % "junit-interface" % Versions.junitInterface
  }

  object Test {
    val scalacheck = "org.scalacheck" %% "scalacheck" % Versions.scalacheck
  }

  object Plugins {
    val sbtCiRelease = "com.github.sbt" % "sbt-ci-release" % Versions.sbtCiRelease
    val sbtScalafmt = "org.scalameta" % "sbt-scalafmt" % Versions.sbtScalafmt
    val sbtJmh = "pl.project13.scala" % "sbt-jmh" % Versions.sbtJmh
    val sbtMdoc = "org.scalameta" % "sbt-mdoc" % Versions.sbtMdoc
    val sbtGithub = "com.alejandrohdezma" %% "sbt-github" % Versions.sbtGithub
    val sbtGithubMdoc = "com.alejandrohdezma" % "sbt-github-mdoc" % Versions.sbtGithubMdoc
    val sbtDependencyUpdates =
      "org.jmotor.sbt" % "sbt-dependency-updates" % Versions.sbtDependencyUpdates
    val sbtExplicitDependencies =
      "com.github.cb372" % "sbt-explicit-dependencies" % Versions.sbtExplicitDependencies
  }

  import Compile._
  import Test._
  import Plugins._

  lazy val dependencies = Seq(
    munitScalacheck,
    scalacheck,
    sbtExplicitDependencies,
    sbtDependencyUpdates,
    sbtGithub,
    sbtGithubMdoc,
    sbtMdoc,
    sbtJmh,
    sbtScalafmt,
    sbtCiRelease,
    munitScalacheck,
    munit,
    junit,
    junitInterface
  )

}
