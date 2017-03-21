import sbt.Keys._
import sbt._
import BuildKeys._
import scoverage.ScoverageKeys._

object Testing {

  import Configs._

  private lazy val testSettings = Seq(
    fork in Test := false,
    parallelExecution in Test := false,
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-verbosity", "2")
  )

  private lazy val e2eSettings = inConfig(EndToEndTest)(Defaults.testSettings) ++ Seq(
    fork in EndToEndTest := false,
    parallelExecution in EndToEndTest := false,
    scalaSource in EndToEndTest := baseDirectory.value / "src/endToEnd/scala"
  )

  private lazy val testAllSettings = Seq(
    testAll :=(),
    testAll <<= testAll.dependsOn(test in EndToEndTest),
    testAll <<= testAll.dependsOn(test in Test)
  )

  private lazy val scoverageSettings = Seq(
    coverageMinimum := 60,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    coverageExcludedPackages := ".*Benchmark"
  )

  lazy val settings = testSettings ++ e2eSettings ++ testAllSettings ++ scoverageSettings
}
