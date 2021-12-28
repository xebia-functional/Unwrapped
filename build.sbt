name := "scala-fx"

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.1.1-RC1"

idePackagePrefix := Some("fx")

run / fork := true

run / connectInput := true

classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

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
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.0",
  "org.scalacheck" %% "scalacheck" % "1.15.4" % "test"
)

lazy val examples = (project in file("examples")).settings(
  name := "examples",
  Compile / unmanagedSourceDirectories += file("src/main/scala"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.3.0",
    "org.http4s" %% "http4s-dsl" % "0.23.6",
    "org.http4s" %% "http4s-blaze-server" % "0.23.6",
    "org.http4s" %% "http4s-circe" % "0.23.6",
    "io.circe" %% "circe-core" % "0.14.1",
    "io.circe" %% "circe-generic" % "0.14.1"
  )
)
