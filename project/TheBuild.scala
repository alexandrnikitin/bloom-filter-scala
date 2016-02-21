import pl.project13.scala.sbt.JmhPlugin
import sbt._

object TheBuild extends Build {

  lazy val root = Project("bloom-filter-root", file("."))
      .aggregate(bloomFilter, tests)
      .configs(Configs.all: _*)
      .settings(Settings.root: _*)

  lazy val bloomFilter = Project("bloom-filter", file("bloom-filter"))
      .configs(Configs.all: _*)
      .settings(Settings.bloomfilter: _*)

  lazy val sandbox = Project("sandbox", file("sandbox"))
      .configs(Configs.all: _*)
      .settings(Settings.sandbox: _*)

  lazy val tests = Project("tests", file("tests"))
      .dependsOn(bloomFilter, sandbox)
      .configs(Configs.all: _*)
      .settings(Settings.tests: _*)

  lazy val benchmark = Project("benchmark", file("benchmark"))
      .dependsOn(bloomFilter, sandbox)
      .configs(Configs.all: _*)
      .settings(Settings.benchmark: _*)
      .enablePlugins(JmhPlugin)

}

