import sbt._

object Configs {
  val EndToEndTest = config("endToEnd") extend Runtime
  val all = EndToEndTest
}