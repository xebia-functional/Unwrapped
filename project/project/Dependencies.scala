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
    val catsEffect = "3.3.12"
    val scalikeJdbc = "4.0.0"
    val h2Database = "2.1.212"
    val logback = "1.2.11"
    val postgres = "42.4.0"
    val testContainers = "0.40.8"
    val flyway = "8.5.12"
    val sttp = "3.6.2"
    val httpCore5 = "5.1.4"
    val hedgehog = "0.9.0"
  }

  object Compile {
    val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    // in compile because munit-scala-fx depends on munit directly
    val munitScalacheck = "org.scalameta" %% "munit-scalacheck" % Versions.munitScalacheck
    // munit transitive dependency conflict
    val junit = "junit" % "junit" % Versions.junit
    val munit = "org.scalameta" %% "munit" % Versions.munit
    val junitInterface = "org.scalameta" % "junit-interface" % Versions.junitInterface
    val scalikejdbc = "org.scalikejdbc" %% "scalikejdbc" % Versions.scalikeJdbc
    val h2Database = "com.h2database" % "h2" % Versions.h2Database
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    val sttp = "com.softwaremill.sttp.client3" %% "core" % Versions.sttp
    val httpCore5 = "org.apache.httpcomponents.core5" % "httpcore5" % Versions.httpCore5

  }

  object Test {
    val scalacheck = "org.scalacheck" %% "scalacheck" % Versions.scalacheck
    val postgres = "org.postgresql" % "postgresql" % Versions.postgres
    val testContainers = "com.dimafeng" %% "testcontainers-scala" % Versions.testContainers
    val testContainersMunit =
      "com.dimafeng" %% "testcontainers-scala-munit" % Versions.testContainers
    val testContainersPostgres =
      "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testContainers
    val flyway = "org.flywaydb" % "flyway-core" % Versions.flyway
    val hedgehog = "qa.hedgehog" %% "hedgehog-munit" % Versions.hedgehog
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
