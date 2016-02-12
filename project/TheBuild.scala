import pl.project13.scala.sbt.JmhPlugin
import sbt._

object TheBuild extends Build {

  lazy val root = Project("bloom-filter-root", file("."))
      .aggregate(bloomFilter, tests, benchmark)
      .configs(Configs.all: _*)
      .settings(Settings.root: _*)

  lazy val bloomFilter = Project("bloom-filter", file("bloom-filter"))
      .configs(Configs.all: _*)
      .settings(Settings.root: _*)

  lazy val tests = Project("tests", file("tests"))
      .dependsOn(bloomFilter)
      .configs(Configs.all: _*)
      .settings(Settings.tests: _*)

  lazy val benchmark = Project("benchmark", file("benchmark"))
      .dependsOn(bloomFilter)
      .configs(Configs.all: _*)
      .settings(Settings.benchmark: _*)
      .enablePlugins(JmhPlugin)

}

