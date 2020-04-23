import sbt.Keys._
import sbt._
import BuildKeys._
import scoverage.ScoverageKeys._

object Testing {

  import Configs._

  private lazy val testSettings = Seq(
    Test / fork := false,
    Test / parallelExecution := false,
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-verbosity", "2")
  )

  private lazy val e2eSettings = inConfig(EndToEndTest)(Defaults.testSettings) ++ Seq(
    EndToEndTest / fork := false,
    EndToEndTest / parallelExecution := false,
    EndToEndTest / scalaSource := baseDirectory.value / "src/endToEnd/scala"
  )

  private lazy val testAllSettings = Seq(
    testAll :=(),
    testAll := testAll.dependsOn(EndToEndTest / test),
    testAll := testAll.dependsOn(Test / test)
  )

  private lazy val scoverageSettings = Seq(
    coverageMinimum := 60,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    coverageExcludedPackages := ".*Benchmark"
  )

  lazy val settings = testSettings ++ e2eSettings ++ testAllSettings ++ scoverageSettings
}
