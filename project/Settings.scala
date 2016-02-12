import sbt.Keys._

object Settings {

  private lazy val build = Seq(
    scalaVersion := "2.11.7"
  )

  lazy val root = build
  lazy val tests = build ++ Testing.settings ++ Dependencies.tests
  lazy val benchmark = build ++ Testing.settings ++ Dependencies.benchmark

}