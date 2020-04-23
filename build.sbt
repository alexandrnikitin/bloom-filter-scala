import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import pl.project13.scala.sbt.JmhPlugin
import sbt._

lazy val root = (project in file("."))
    .aggregate(bloomFilter, tests, examples)
    .configs(Configs.all)
    .settings(name := "bloom-filter-root", Settings.root)
    .enablePlugins()

lazy val bloomFilter = (project in file("bloom-filter"))
    .configs(Configs.all)
    .settings(name := "bloom-filter", Settings.bloomfilter)

lazy val sandbox = (project in file("sandbox"))
    .dependsOn(bloomFilter)
    .configs(Configs.all)
    .settings(Settings.sandbox)

lazy val sandboxApp = (project in file("sandboxApp"))
    .dependsOn(bloomFilter)
    .configs(Configs.all)
    .settings(Settings.sandboxApp)
    .enablePlugins(JavaAppPackaging)

lazy val tests = (project in file("tests"))
    .dependsOn(bloomFilter, sandbox)
    .configs(Configs.all)
    .settings(Settings.tests)

lazy val benchmarks = (project in file("benchmarks"))
    .dependsOn(bloomFilter, sandbox)
    .configs(Configs.all)
    .settings(Settings.benchmarks)
    .enablePlugins(JmhPlugin)

lazy val examples = (project in file("examples"))
    .dependsOn(bloomFilter)
    .configs(Configs.all)
    .settings(Settings.examples)
    .enablePlugins(JavaAppPackaging)
