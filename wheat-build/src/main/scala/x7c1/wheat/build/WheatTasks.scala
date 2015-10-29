package x7c1.wheat.build

import sbt.InputKey

object WheatTasks {
  def settings = Seq(
    InputKey[Unit]("generate-layout") := LayoutGenerator.task.evaluated
  )
}
