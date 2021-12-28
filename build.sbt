ThisBuild / scalaVersion := "3.1.1-RC1"
ThisBuild / organization := "com.47deg"
ThisBuild / versionScheme := Some("early-semver")

addCommandAlias("ci-test", "scalafmtCheckAll; scalafmtSbtCheck; mdoc; test")
addCommandAlias("ci-docs", "github; mdoc")
addCommandAlias("ci-publish", "github; ci-release")

publish / skip := true

lazy val `scala-fx` = project.settings(scalafxSettings: _*)

lazy val benchmarks =
  project.dependsOn(`scala-fx`).settings(publish / skip := true).enablePlugins(JmhPlugin)

lazy val documentation = project
  .dependsOn(`scala-fx`)
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))
  .settings(publish / skip := true)

lazy val scalafxSettings: Seq[Def.Setting[_]] =
  Seq(
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    javaOptions ++= Seq(
      "-XX:+IgnoreUnrecognizedVMOptions",
      "-XX:-DetectLocksInCompiledFrames",
      "-XX:+UnlockDiagnosticVMOptions",
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseNewCode",
      "--add-modules=java.base",
      "--add-opens java.base/jdk.internal.vm=ALL-UNNAMED",
      "--add-exports java.base/jdk.internal.vm=ALL-UNNAMED",
      "--enable-preview"
    ),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.15.4" % Test
    )
  )

lazy val examples = project
  .dependsOn(`scala-fx`)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.0",
      "org.http4s" %% "http4s-dsl" % "0.23.6",
      "org.http4s" %% "http4s-blaze-server" % "0.23.6",
      "org.http4s" %% "http4s-circe" % "0.23.6",
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1"
    )
  )
