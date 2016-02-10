import sbt.Keys._
import sbt._

object Dependencies {
  private val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test;bt;e2e"

  val benchmark = dependencies(scalatest)

  private def dependencies(modules: ModuleID*): Seq[Setting[_]] = Seq(libraryDependencies ++= modules)
}