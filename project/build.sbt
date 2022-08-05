ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.47Deg"
ThisBuild / homepage := Some(url("https://47Deg.com"))

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-hello",
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.13.3",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8" // set minimum sbt version
      }
    }
  )
