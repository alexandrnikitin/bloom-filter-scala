import sbt.Keys._

object Settings {

  private lazy val build = Seq(
    scalaVersion := "2.11.7",
    organization := "com.github.alexandrnikitin"
  )

  lazy val root = build ++ Testing.settings ++ Publishing.noPublishSettings
  lazy val bloomfilter = build ++ Testing.settings ++ Publishing.settings
  lazy val tests = build ++ Testing.settings ++ Dependencies.tests
  lazy val benchmark = build ++ Testing.settings ++ Dependencies.benchmark

}