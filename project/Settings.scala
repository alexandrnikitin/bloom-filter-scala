import sbt._
import sbt.Keys._

object Settings {

  private lazy val build = Seq(
    scalaVersion := "2.11.7",

    autoCompilerPlugins := true,
    addCompilerPlugin("com.github.ghik" % "silencer-plugin" % "0.3"),

    scalacOptions ++= commonScalacOptions,
    organization := "com.github.alexandrnikitin"
  )

  lazy val root = build ++ Testing.settings ++ Publishing.noPublishSettings
  lazy val bloomfilter = build ++ Testing.settings ++ Dependencies.bloomfilter ++ Publishing.settings
  lazy val tests = build ++ Testing.settings ++ Dependencies.tests
  lazy val benchmark = build ++ Testing.settings ++ Dependencies.benchmark

  lazy val commonScalacOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )

}