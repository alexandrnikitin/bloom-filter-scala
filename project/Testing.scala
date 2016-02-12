import sbt.Keys._
import sbt._
import BuildKeys._

object Testing {

  import Configs._

  private lazy val testSettings = Seq(
    fork in Test := false,
    parallelExecution in Test := false
  )

  private lazy val e2eSettings = inConfig(EndToEndTest)(Defaults.testSettings) ++ Seq(
    fork in EndToEndTest := false,
    parallelExecution in EndToEndTest := false,
    scalaSource in EndToEndTest := baseDirectory.value / "src/e2e/scala"
  )

  private lazy val btSettings = inConfig(BenchmarkTest)(Defaults.testSettings) ++ Seq(
    fork in BenchmarkTest := false,
    parallelExecution in BenchmarkTest := false,
    scalaSource in BenchmarkTest := baseDirectory.value / "src/test/scala"
    //testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
    // TODO add JMH test framework
  )

  lazy val settings = testSettings ++ e2eSettings ++ btSettings ++ Seq(
    testAll :=(),
    testAll <<= testAll.dependsOn(test in EndToEndTest),
    testAll <<= testAll.dependsOn(test in Test)
  )
}
