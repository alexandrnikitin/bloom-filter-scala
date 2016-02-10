import sbt._

object Configs {
  val EndToEndTest = config("e2e") extend Runtime
  val BenchmarkTest = config("bt") extend Runtime
  val all = Seq(EndToEndTest, BenchmarkTest)
}