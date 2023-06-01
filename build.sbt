import Dependencies.Compile._
import Dependencies.Test._

ThisBuild / scalaVersion := "3.1.2"
ThisBuild / organization := "com.47deg"
ThisBuild / versionScheme := Some("early-semver")

addCommandAlias("ci-test", "scalafmtCheckAll; scalafmtSbtCheck; github; mdoc; root / test")
addCommandAlias("ci-docs", "github; mdoc")
addCommandAlias("ci-publish", "github; ci-release")

lazy val root =
  (project in file("./"))
    .settings(publish / skip := true)
    .aggregate(
      benchmarks,
      documentation,
      `http-unwrapped`,
      `java-net-multipart-body-publisher`,
      `munit-unwrapped`,
      `unwrapped`,
      `scalike-jdbc-unwrapped`,
      `sttp-unwrapped`
    )

lazy val `unwrapped` = project.settings(unwrappedSettings: _*)

lazy val benchmarks =
  project.dependsOn(`unwrapped`).settings(publish / skip := true).enablePlugins(JmhPlugin)

lazy val documentation = project
  .dependsOn(`unwrapped`)
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))
  .settings(publish / skip := true)

lazy val `munit-unwrapped` = (project in file("./munit-unwrapped"))
  .configs(IntegrationTest)
  .settings(
    munitUnwrappedSettings
  )
  .dependsOn(`unwrapped`)

lazy val `cats-unwrapped` = (project in file("./cats-unwrapped"))
  .configs(IntegrationTest)
  .settings(
    catsUnwrappedSettings
  )
  .dependsOn(`unwrapped`)

lazy val `scalike-jdbc-unwrapped` = project
  .dependsOn(`unwrapped`, `munit-unwrapped` % "test -> compile")
  .settings(scalalikeSettings)

lazy val `java-net-multipart-body-publisher` =
  (project in file("./java-net-mulitpart-body-publisher")).settings(commonSettings)

lazy val `http-unwrapped` = (project in file("./http-unwrapped"))
  .settings(httpUnwrappedSettings)
  .settings(generateMediaTypeSettings)
  .dependsOn(
    `java-net-multipart-body-publisher`,
    `unwrapped`,
    `munit-unwrapped` % "test -> compile")
  .enablePlugins(HttpUnwrappedPlugin)

lazy val `sttp-unwrapped` = (project in file("./sttp-unwrapped"))
  .settings(sttpUnwrappedSettings)
  .dependsOn(
    `java-net-multipart-body-publisher`,
    `unwrapped`,
    `http-unwrapped`,
    `munit-unwrapped` % "test -> compile")

lazy val commonSettings = Seq(
  javaOptions ++= javaOptionsSettings,
  autoAPIMappings := true,
  Test / fork := true
)

lazy val unwrappedSettings: Seq[Def.Setting[_]] =
  Seq(
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    javaOptions ++= javaOptionsSettings,
    autoAPIMappings := true,
    libraryDependencies ++= Seq(
      scalacheck % Test
    )
  )

lazy val munitUnwrappedSettings = Defaults.itSettings ++ Seq(
  libraryDependencies ++= Seq(
    munitScalacheck,
    hedgehog,
    junit,
    munit,
    junitInterface
  )
) ++ commonSettings

lazy val catsUnwrappedSettings = Seq(
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

lazy val httpUnwrappedSettings = commonSettings

lazy val sttpUnwrappedSettings = commonSettings ++ Seq(
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
