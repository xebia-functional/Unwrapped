import Dependencies.Compile._
import Dependencies.Test._

import scala.util.Properties

ThisBuild / scalaVersion := "3.1.2"
ThisBuild / organization := "com.47deg"
ThisBuild / versionScheme := Some("early-semver")

addCommandAlias("ci-test", "scalafmtCheckAll; scalafmtSbtCheck; github; mdoc; test")
addCommandAlias("ci-docs", "github; mdoc")
addCommandAlias("ci-publish", "github; ci-release")

publish / skip := true

lazy val root =
  (project in file("./")).aggregate(`scala-fx`, continuationsPlugin, continuationsPluginExample, benchmarks, `munit-scala-fx`, documentation)

lazy val `scala-fx` = project.settings(scalafxSettings: _*)


lazy val continuationsPlugin = project
  .settings(
    continuationsPluginSettings: _*
  )

lazy val continuationsPluginExample = project
  .dependsOn(continuationsPlugin)
  .settings(
    continuationsPluginExampleSettings: _*
  )


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

lazy val scalafxSettings: Seq[Def.Setting[_]] =
  Seq(
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    javaOptions ++= javaOptionsSettings,
    autoAPIMappings := true,
    libraryDependencies ++= Seq(
      scalacheck % Test
    )
  )

lazy val continuationsPluginSettings: Seq[Def.Setting[_]] =
  Seq(
    libraryDependencies ++= List(
      "org.scala-lang" %% "scala3-compiler" % "3.1.2"
    )
  )

lazy val continuationsPluginExampleSettings: Seq[Def.Setting[_]] =
  Seq(
    publish / skip := true,
    autoCompilerPlugins := true,
    resolvers += Resolver.mavenLocal,
    // this uses the mac system environment variable, HOME. Mine is included by default as an example
    Compile / scalacOptions += s"-Xplugin:${Properties.envOrElse("HOME", "/Users/jackcviers")}/.ivy2/local/com.47deg/continuationsplugin_3/0.0.0+43-bede6404-SNAPSHOT/jars/continuationsplugin_3.jar"
    // addCompilerPlugin("com.47deg" %% "continuationsplugin" % "+" changing())
  )


lazy val munitScalaFXSettings = Defaults.itSettings ++ Seq(
  Test / fork := true,
  javaOptions ++= javaOptionsSettings,
  autoAPIMappings := true,
  libraryDependencies ++= Seq(
    munitScalacheck,
    junit,
    munit,
    junitInterface
  )
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

