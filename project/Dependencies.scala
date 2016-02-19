import sbt.Keys._
import sbt._

object Dependencies {
  private val silencer = "com.github.ghik" % "silencer-lib" % "0.3"
  private val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test;bt;e2e"

  private val common = dependencies(silencer)

  val bloomfilter = common
  val tests = common ++ dependencies(scalatest)
  val benchmark = common ++ dependencies(scalatest)

  private def dependencies(modules: ModuleID*): Seq[Setting[_]] = Seq(libraryDependencies ++= modules)
}