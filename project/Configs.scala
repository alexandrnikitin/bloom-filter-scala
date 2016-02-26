import sbt._

object Configs {
  val EndToEndTest = config("endToEnd") extend Runtime
  val BenchmarkTest = config("bt") extend Runtime
  val all = Seq(EndToEndTest, BenchmarkTest)
}