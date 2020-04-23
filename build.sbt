import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import pl.project13.scala.sbt.JmhPlugin
import sbt._

lazy val root = (project in file("."))
    .aggregate(bloomFilter, tests, examples)
    .configs(Configs.all: _*)
    .settings(Settings.root: _*)
    .enablePlugins()

lazy val bloomFilter = (project in file("bloom-filter"))
    .configs(Configs.all: _*)
    .settings(Settings.bloomfilter: _*)

lazy val sandbox = (project in file("sandbox"))
    .dependsOn(bloomFilter)
    .configs(Configs.all: _*)
    .settings(Settings.sandbox: _*)

lazy val sandboxApp = (project in file("sandboxApp"))
    .dependsOn(bloomFilter)
    .configs(Configs.all: _*)
    .settings(Settings.sandboxApp: _*)
    .enablePlugins(JavaAppPackaging)

lazy val tests = (project in file("tests"))
    .dependsOn(bloomFilter, sandbox)
    .configs(Configs.all: _*)
    .settings(Settings.tests: _*)

lazy val benchmarks = (project in file("benchmarks"))
    .dependsOn(bloomFilter, sandbox)
    .configs(Configs.all: _*)
    .settings(Settings.benchmarks: _*)
    .enablePlugins(JmhPlugin)

lazy val examples = (project in file("examples"))
    .dependsOn(bloomFilter)
    .configs(Configs.all: _*)
    .settings(Settings.examples: _*)
    .enablePlugins(JavaAppPackaging)
