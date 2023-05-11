import Dependencies.Compile._
import Dependencies.Test._

ThisBuild / scalaVersion := "3.1.2"
ThisBuild / organization := "com.47deg"
ThisBuild / versionScheme := Some("early-semver")

addCommandAlias(
  "plugin-example",
  "reload; clean; publishLocal; continuationsPluginExample/compile")
addCommandAlias("ci-test", "scalafmtCheckAll; scalafmtSbtCheck; github; mdoc; root / test")
addCommandAlias("ci-it-test", "continuationsPlugin / IntegrationTest / test")
addCommandAlias("ci-docs", "github; mdoc")
addCommandAlias("ci-publish", "github; ci-release")

lazy val root =
  (project in file("./"))
    .settings(publish / skip := true)
    .aggregate(
      benchmarks,
      continuationsPlugin,
      documentation,
      `http-unwrapped`,
      `java-net-multipart-body-publisher`,
      `munit-unwrapped`,
      `unwrapped`,
      `scalike-jdbc-unwrapped`,
      `sttp-unwrapped`,
      `munit-snap`
    )

lazy val bypassZinc = (project in file("./bypassZinc"))
  .settings(publish / skip := true)
  .aggregate(
    continuationsPluginExample,
    `zero-arguments-no-continuation-treeview`,
    `zero-arguments-one-continuation-code-before-used-after`,
    `list-map`,
    `two-arguments-two-continuations`
  )

lazy val `unwrapped` = project.settings(unwrappedSettings: _*)

lazy val continuationsPlugin = project
  .configs(IntegrationTest)
  .settings(
    continuationsPluginSettings: _*
  )
  .dependsOn(`munit-snap`)

lazy val continuationsPluginExample = project
  .dependsOn(continuationsPlugin)
  .settings(
    continuationsPluginExampleSettings: _*
  )
  .enablePlugins(ForceableCompilationPlugin)

lazy val `zero-arguments-no-continuation-treeview` =
  (project in file("./zero-arguments-no-continuation-treeview"))
    .settings(continuationsPluginExampleShowTreeSettings: _*)
    .dependsOn(continuationsPlugin)
    .enablePlugins(ForceableCompilationPlugin)

lazy val `zero-arguments-one-continuation-code-before-used-after` =
  (project in file("./zero-arguments-one-continuation-code-before-used-after"))
    .settings(continuationsPluginExampleShowTreeSettings: _*)
    .dependsOn(continuationsPlugin)
    .enablePlugins(ForceableCompilationPlugin)

lazy val `list-map` = (project in file("./list-map"))
  .settings(continuationsPluginExampleShowTreeSettings: _*)
  .dependsOn(continuationsPlugin)
  .enablePlugins(ForceableCompilationPlugin)

lazy val `two-arguments-two-continuations` =
  (project in file("./two-arguments-two-continuations"))
    .settings(continuationsPluginExampleShowTreeSettings: _*)
    .dependsOn(continuationsPlugin)
    .enablePlugins(ForceableCompilationPlugin)

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

lazy val `munit-snap` = (project in file("./munit-snap")).settings(munitSnapSettings)

lazy val munitSnapSettings = Seq(
  name := "munit-snap",
  autoAPIMappings := true,
  Test / fork := true,
  libraryDependencies += munit,
  libraryDependencies += circe,
  libraryDependencies += circeParser,
  libraryDependencies += circeGeneric % Test
)

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

def testAndIntegrationTest(m: ModuleID): List[ModuleID] = List(m).flatMap { m =>
  List(m % Test, m % IntegrationTest)
}

lazy val continuationsPluginSettings: Seq[Def.Setting[_]] =
  Defaults.itSettings ++ Seq(
    exportJars := true,
    autoAPIMappings := true,
    publish / skip := true,
    Test / fork := true,
    libraryDependencies ++= List(
      "org.scala-lang" %% "scala3-compiler" % "3.1.2"
    ) ++ testAndIntegrationTest(munit),
    Test / javaOptions += {
      val `scala-compiler-classpath` =
        (Compile / dependencyClasspath)
          .value
          .files
          .map(_.toPath().toAbsolutePath().toString())
          .mkString(":")
      s"-Dscala-compiler-classpath=${`scala-compiler-classpath`}"
    },
    Test / javaOptions += {
      s"""-Dcompiler-scalacOptions=\"${scalacOptions.value.mkString(" ")}\""""
    },
    Test / javaOptions += Def.taskDyn {
      Def.task {
        val _ = (Compile / Keys.`package`).value
        val `scala-compiler-options` =
          s"${(continuationsPlugin / Compile / packageBin).value}"
        s"""-Dscala-compiler-plugin=${`scala-compiler-options`}"""
      }
    }.value,
    IntegrationTest / fork := true,
    IntegrationTest / javaOptions := (Test / javaOptions).value
  )

lazy val continuationsPluginExampleShowTreeSettings: Seq[Def.Setting[_]] =
  Seq(
    publish / skip := true,
    autoCompilerPlugins := true,
    resolvers += Resolver.mavenLocal,
    forceCompilation := false,
    Compile / scalacOptions += s"-Xplugin:${(continuationsPlugin / Compile / packageBin).value}",
    Compile / scalacOptions += "-Xprint:all",
    Test / scalacOptions += s"-Xplugin:${(continuationsPlugin / Compile / packageBin).value}",
    Test / scalacOptions += "-Xprint:all"
  )

lazy val continuationsPluginExampleSettings: Seq[Def.Setting[_]] =
  Seq(
    publish / skip := true,
    autoCompilerPlugins := true,
    forceCompilation := true,
    resolvers += Resolver.mavenLocal,
    Compile / scalacOptions += s"-Xplugin:${(continuationsPlugin / Compile / packageBin).value}",
    Test / scalacOptions += s"-Xplugin:${(continuationsPlugin / Compile / packageBin).value}"
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
