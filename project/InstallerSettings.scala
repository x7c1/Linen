import InstallerError.{DeviceNotConnected, UnknownFormatLine}
import sbt.Def.{SettingList, SettingsDefinition, taskKey}
import sbt.Keys.streams
import sbt.{Def, Project, TaskKey, stringToProcess}
import sbtassembly.AssemblyKeys.assembly
import x7c1.wheat.splicer.lib.HasProcessLogger.LogReader
import x7c1.wheat.splicer.lib.LogMessage.{Error, Info}
import x7c1.wheat.splicer.lib.{HasProcessLogger, LogMessage, ProcessRunner, Reader}

import scala.util.{Left, Right}


object InstallerSettings {

  lazy val installAfterAssemble: TaskKey[Unit] = {
    taskKey("Build Android app and install to connected device.")
  }
  lazy val installApp: TaskKey[Unit] = {
    taskKey("Install Android app to connected device.")
  }

  def definition(project: Project): SettingsDefinition =
    new SettingList(Seq(
      installApp := {
        installTask run streams.value.log
      },
      installAfterAssemble := Def.sequential(
        assembly in project,
        installApp
      ).value
    ))

  private def installTask: Reader[HasProcessLogger, Unit] = {
    findDevice flatMap {
      case Left(error) => LogReader(_ error error.message)
      case Right(device) => runApp(device)
    }
  }

  private def findDevice = LogReader { logger =>
    val lines = "adb devices".lines_!(logger).toIndexedSeq
    lines filter (_.nonEmpty) match {
      case head +: line +: _ => line.split("\t").toSeq match {
        case device +: _ if device.nonEmpty =>
          Right(device)
        case _ =>
          Left(UnknownFormatLine(line))
      }
      case _ => Left(DeviceNotConnected())
    }
  }

  private def runApp(device: String): Reader[HasProcessLogger, Unit] = {
    val toMessage: String => Int => LogMessage = label => {
      case 0 => Info(s"[done] $label: device=$device")
      case n => Error(s"[failed] (code:$n) $label: device=$device")
    }
    val readers = Seq(
      assemble.reader map toMessage("assemble"),
      transfer(device).reader map toMessage("transfer"),
      launch(device).reader map toMessage("launch")
    )
    readers.map(_ flatMap (_.toReader)).uniteAll
  }

  def assemble = ProcessRunner(Seq(
    "./gradlew", "--daemon", "--parallel", "assembleDebug"
  ))

  def transfer(device: String) = ProcessRunner(Seq(
    "adb", "-s", device,
    "install", "-r", "./linen-starter/build/outputs/apk/linen-starter-debug.apk"
  ))

  def launch(device: String) = ProcessRunner(Seq(
    "adb", "-s", device,
    "shell", "am", "start", "-n", "x7c1.linen/x7c1.linen.unread.UnreadItemsActivity"
  ))
}
