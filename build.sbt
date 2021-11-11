name := "scala-fx"

version := "0.1"

scalaVersion := "3.1.0"

idePackagePrefix := Some("fx")

javaOptions ++= Seq(
  // "-source 18",
  "-target 18",
  "-XX:-DetectLocksInCompiledFrames",
  "-XX:+UnlockDiagnosticVMOptions",
  "-XX:+UnlockExperimentalVMOptions",
  "-XX:+UseNewCode",
  "--enable-preview")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.2.9",
  "org.scalacheck" %% "scalacheck" % "1.15.4" % "test"
)
