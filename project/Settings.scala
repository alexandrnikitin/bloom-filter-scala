import sbt._
import sbt.Keys._

object Settings {

  private lazy val build = Seq(
    scalaVersion := "2.12.11",
    crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.11", "2.13.1"),

    autoCompilerPlugins := true,

    scalacOptions ++= ScalacSettings.base ++ ScalacSettings.specificFor(scalaVersion.value),
    javacOptions ++= JavacSettings.base ++ JavacSettings.specificFor(scalaVersion.value),
    javaOptions += "-Xmx1G",
    organization := "com.github.alexandrnikitin"
  )

  lazy val root = build ++ Testing.settings ++ Publishing.noPublishSettings
  lazy val bloomfilter = build ++ Testing.settings ++ Dependencies.bloomfilter ++ Publishing.settings ++
      (scalacOptions ++= ScalacSettings.strictBase ++ ScalacSettings.strictSpecificFor(scalaVersion.value))
  lazy val sandbox = build ++ Testing.settings ++ Dependencies.sandbox ++ Publishing.noPublishSettings
  lazy val sandboxApp = build ++ Dependencies.sandboxApp ++ Publishing.noPublishSettings
  lazy val tests = build ++ Testing.settings ++ Dependencies.tests ++ Publishing.noPublishSettings
  lazy val benchmarks = build ++ Dependencies.benchmarks ++ Publishing.noPublishSettings
  lazy val examples = build ++ Publishing.noPublishSettings

  object JavacSettings {
    val base = Seq("-Xlint")

    def specificFor(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) => Seq("-source", "1.8", "-target", "1.8")
      case Some((2, 12)) => Seq("-source", "1.8", "-target", "1.8")
      case Some((2, 11)) => Seq("-source", "1.8", "-target", "1.8")
      case Some((2, 10)) => Seq("-source", "1.7", "-target", "1.7")
      case _ => Nil
    }
  }

  object ScalacSettings {
    val base = Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked"
    )

    def specificFor(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => Seq("-target:jvm-1.8")
      case Some((2, 11)) => Seq("-target:jvm-1.8", "-optimise")
      case Some((2, 10)) => Seq("-target:jvm-1.7", "-optimise")
      case _ => Nil
    }


    val strictBase = Seq(
      "-Xfatal-warnings",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard"
    )

    def strictSpecificFor(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => Seq("-Ywarn-unused", "-Ywarn-unused-import")
      case Some((2, 11)) => Seq("-Ywarn-unused", "-Ywarn-unused-import")
      case _ => Nil
    }

  }

}
