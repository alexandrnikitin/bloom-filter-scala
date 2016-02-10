import sbt.TaskKey

object BuildKeys {
  lazy val testAll = TaskKey[Unit]("test-all")
}
