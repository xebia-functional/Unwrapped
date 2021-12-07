name := "scala-fx"

version := "0.1"

scalaVersion := "3.1.0"

idePackagePrefix := Some("fx")

run / fork := true

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
  "--enable-preview")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.0",
  "org.scalacheck" %% "scalacheck" % "1.15.4" % "test"
)
