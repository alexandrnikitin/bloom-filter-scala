import sbt.Keys._

object Settings {

  private lazy val build = Seq(
    name := "bloom-filter-scala",
    scalaVersion := "2.11.7"
  )

  lazy val root = build
  lazy val benchmark = build ++ Dependencies.benchmark

}