import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import pl.project13.scala.sbt.JmhPlugin
import sbt._

object TheBuild extends Build {

  lazy val root = Project("bloom-filter-root", file("."))
      .aggregate(bloomFilter, tests, examples)
      .configs(Configs.all: _*)
      .settings(Settings.root: _*)

  lazy val bloomFilter = Project("bloom-filter", file("bloom-filter"))
      .configs(Configs.all: _*)
      .settings(Settings.bloomfilter: _*)

  lazy val sandbox = Project("sandbox", file("sandbox"))
      .configs(Configs.all: _*)
      .settings(Settings.sandbox: _*)

  lazy val sandboxApp = Project("sandboxApp", file("sandboxApp"))
      .configs(Configs.all: _*)
      .settings(Settings.sandboxApp: _*)

  lazy val tests = Project("tests", file("tests"))
      .dependsOn(bloomFilter, sandbox)
      .configs(Configs.all: _*)
      .settings(Settings.tests: _*)

  lazy val benchmarks = Project("benchmarks", file("benchmarks"))
      .dependsOn(bloomFilter, sandbox)
      .configs(Configs.all: _*)
      .settings(Settings.benchmarks: _*)
      .enablePlugins(JmhPlugin)

  lazy val examples = Project("examples", file("examples"))
      .dependsOn(bloomFilter)
      .configs(Configs.all: _*)
      .settings(Settings.examples: _*)
      .enablePlugins(JavaAppPackaging)

}

