import Dependencies.Compile._
import Dependencies.Test._

import scala.collection.JavaConverters._

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser

ThisBuild / scalaVersion := "3.1.2"
ThisBuild / organization := "com.47deg"
ThisBuild / versionScheme := Some("early-semver")

addCommandAlias("ci-test", "scalafmtCheckAll; scalafmtSbtCheck; github; mdoc; test")
addCommandAlias("ci-docs", "github; mdoc")
addCommandAlias("ci-publish", "github; ci-release")

publish / skip := true

lazy val root =
  (project in file("./")).aggregate(
    `scala-fx`,
    benchmarks,
    `munit-scala-fx`,
    `scalike-jdbc-scala-fx`,
    `http-scala-fx`,
    documentation,
    `sttp-scala-fx`,
    `java-net-multipart-body-publisher`
  )

lazy val `scala-fx` = project.settings(scalafxSettings: _*)

lazy val benchmarks =
  project.dependsOn(`scala-fx`).settings(publish / skip := true).enablePlugins(JmhPlugin)

lazy val documentation = project
  .dependsOn(`scala-fx`)
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))
  .settings(publish / skip := true)

lazy val `munit-scala-fx` = (project in file("./munit-scalafx"))
  .configs(IntegrationTest)
  .settings(
    munitScalaFXSettings
  )
  .dependsOn(`scala-fx`)

lazy val `cats-scala-fx` = (project in file("./cats-scalafx"))
  .configs(IntegrationTest)
  .settings(
    catsScalaFXSettings
  )
  .dependsOn(`scala-fx`)

lazy val `scalike-jdbc-scala-fx` = project
  .dependsOn(`scala-fx`, `munit-scala-fx` % "test -> compile")
  .settings(publish / skip := true)
  .settings(scalalikeSettings)

lazy val `java-net-multipart-body-publisher` =
  (project in file("./java-net-mulitpart-body-publisher")).settings(commonSettings)

lazy val `http-scala-fx` = (project in file("./http-scala-fx"))
  .settings(httpScalaFXSettings)
  .settings(generateMediaTypeSettings)
  .dependsOn(
    `java-net-multipart-body-publisher`,
    `scala-fx`,
    `munit-scala-fx` % "test -> compile")
  .enablePlugins(HttpScalaFxPlugin)

lazy val `sttp-scala-fx` = (project in file("./sttp-scala-fx"))
  .settings(sttpScalaFXSettings)
  .dependsOn(
    `java-net-multipart-body-publisher`,
    `scala-fx`,
    `http-scala-fx`,
    `munit-scala-fx` % "test -> compile")

lazy val commonSettings = Seq(
  javaOptions ++= javaOptionsSettings,
  autoAPIMappings := true,
  Test / fork := true
)

lazy val scalafxSettings: Seq[Def.Setting[_]] =
  Seq(
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    javaOptions ++= javaOptionsSettings,
    autoAPIMappings := true,
    libraryDependencies ++= Seq(
      scalacheck % Test
    )
  )

lazy val munitScalaFXSettings = Defaults.itSettings ++ Seq(
  libraryDependencies ++= Seq(
    munitScalacheck,
    hedgehog,
    junit,
    munit,
    junitInterface
  )
) ++ commonSettings

lazy val catsScalaFXSettings = Seq(
  classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
  javaOptions ++= javaOptionsSettings,
  autoAPIMappings := true,
  libraryDependencies ++= Seq(
    catsEffect,
    scalacheck % Test
  )
)

lazy val scalalikeSettings: Seq[Def.Setting[_]] =
  Seq(
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    javaOptions ++= javaOptionsSettings,
    autoAPIMappings := true,
    libraryDependencies ++= Seq(
      scalikejdbc,
      h2Database,
      logback,
      postgres,
      scalacheck % Test,
      testContainers % Test,
      testContainersMunit % Test,
      testContainersPostgres % Test,
      flyway % Test
    )
  )

lazy val httpScalaFXSettings = commonSettings

lazy val sttpScalaFXSettings = commonSettings ++ Seq(
  libraryDependencies += sttp,
  libraryDependencies += httpCore5,
  libraryDependencies += hedgehog % Test
)

lazy val javaOptionsSettings = Seq(
  "-XX:+IgnoreUnrecognizedVMOptions",
  "-XX:-DetectLocksInCompiledFrames",
  "-XX:+UnlockDiagnosticVMOptions",
  "-XX:+UnlockExperimentalVMOptions",
  "-XX:+UseNewCode",
  "--add-modules=java.base",
  "--add-modules=jdk.incubator.concurrent",
  "--add-opens java.base/jdk.internal.vm=ALL-UNNAMED",
  "--add-exports java.base/jdk.internal.vm=ALL-UNNAMED",
  "--enable-preview"
)
