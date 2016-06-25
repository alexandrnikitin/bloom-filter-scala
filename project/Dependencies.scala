import sbt.Keys._
import sbt._

object Dependencies {
  private val silencer = "com.github.ghik" % "silencer-lib" % "0.3"
  private val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test;endToEnd"
  private val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.0" % "test"
  private val googleGuava = "com.google.guava" % "guava" % "19.0"
  private val googleFindbugs = "com.google.code.findbugs" % "jsr305" % "2.0.3" // needed by guava
  private val breeze = "org.scalanlp" %% "breeze" % "0.12"
  private val breezeNatives = "org.scalanlp" %% "breeze-natives" % "0.12"
  private val algebird = "com.twitter" %% "algebird-core" % "0.11.0"
  private val sketches = "com.yahoo.datasketches" % "sketches-core" % "0.3.2"
  private val chronicleBytes = "net.openhft" % "chronicle-bytes" % "1.2.3"
  private val allocationInstrumenter = "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "3.0.1"

  private val common = dependencies(silencer)

  val bloomfilter = common
  val sandbox = common ++ dependencies(chronicleBytes)
  val sandboxApp = common ++ dependencies(allocationInstrumenter)
  val tests = common ++ dependencies(scalatest, scalacheck)
  val benchmarks = common ++ dependencies(googleGuava, googleFindbugs, breeze, breezeNatives, algebird, sketches)

  private def dependencies(modules: ModuleID*): Seq[Setting[_]] = Seq(libraryDependencies ++= modules)
}