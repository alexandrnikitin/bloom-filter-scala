import sbt._
import sbt.Keys._

object Settings {

  private lazy val build = Seq(
    scalaVersion := "2.11.7",

    autoCompilerPlugins := true,
    addCompilerPlugin("com.github.ghik" % "silencer-plugin" % "0.3"),

    scalacOptions ++= ScalacSettings.base,
    javacOptions ++= JavacSettings.base,
    organization := "com.github.alexandrnikitin"
  )

  lazy val root = build ++ Testing.settings ++ Publishing.noPublishSettings
  lazy val bloomfilter = build ++ Testing.settings ++ Dependencies.bloomfilter ++ Publishing.settings ++ (scalacOptions ++= ScalacSettings.strict)
  lazy val sandbox = build ++ Testing.settings ++ Dependencies.sandbox ++ Publishing.noPublishSettings
  lazy val tests = build ++ Testing.settings ++ Dependencies.tests ++ Publishing.noPublishSettings
  lazy val benchmarks = build ++ Dependencies.benchmarks ++ Publishing.noPublishSettings
  lazy val examples = build ++ Publishing.noPublishSettings

  object JavacSettings {
    val base = Seq("-source", "1.8", "-target", "1.8", "-Xlint")
  }

  object ScalacSettings {
    val base = Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-optimise",
      "-target:jvm-1.8"
    )

    val strict = Seq(
      "-Xfatal-warnings",
      "-Xlint",
      "-Ywarn-unused",
      "-Ywarn-unused-import",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard"
    )
  }

}