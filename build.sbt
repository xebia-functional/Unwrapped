ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.1.1-RC1"

publish / skip := true

lazy val `scala-fx` = project.settings(scalafxSettings: _*)

lazy val benchmarks = project.dependsOn(`scala-fx`).enablePlugins(JmhPlugin)

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
