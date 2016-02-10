import sbt._

object TheBuild extends Build {

  lazy val root = Project("root", file("."))
      .aggregate(bloomFilter, benchmark)
      .settings(Settings.root: _*)

  lazy val bloomFilter = Project("bloom-filter", file("bloom-filter"))
      .settings(Settings.root: _*)

  lazy val benchmark = Project("benchmark", file("benchmark"))
      .settings(Settings.root: _*)

}

