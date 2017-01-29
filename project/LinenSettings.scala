import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.MergeStrategy
import x7c1.wheat.harvest.{WheatDirectories, WheatPackages}

object LinenSettings {

  lazy val testLibrary = "org.scalatest" %% "scalatest" % "3.0.1" % Test

  lazy val linenSettings = Seq(
    scalaVersion := "2.12.1",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint"
    ),
    libraryDependencies += testLibrary,
    logLevel in assembly := Level.Error
  )

  lazy val linenPackages = WheatPackages(
    starter = "x7c1.linen",
    starterLayout = "x7c1.linen.res.layout",
    starterValues = "x7c1.linen.res.values",
    glueLayout = "x7c1.linen.glue.res.layout",
    glueValues = "x7c1.linen.glue.res.values"
  )

  lazy val linenDirectories = WheatDirectories(
    starter = file("linen-starter"),
    glue = file("linen-glue")
  )

  lazy val discardTargets: Def.Initialize[String => MergeStrategy] = {
    val ignore = {
      val prefixes = Seq(
        "org/jdom", "JDOMAbout",
        "com/google/code/rome", "META-INF",
        "com/typesafe/config",
        "org/jsoup",
        "x7c1/linen/glue", "x7c1/wheat/ancient"
      )
      prefixes exists (_: String).startsWith
    }
    Def.setting {
      case path if ignore(path) => MergeStrategy.discard
      case path =>
        if (!(path startsWith "x7c1")) {
          sLog.value info s"may be duplicate between linen-pickle and linen-modern : $path"
        }
        val original = (assemblyMergeStrategy in assembly).value
        original(path)
    }
  }

  lazy val assembleAndInstall =
    Def.taskKey[Unit]("Build android app and install to connected device.")

  def linenTasks(project: Project) = Seq(
    assembleAndInstall := Def.sequential(
      assembly in project,
      installTask
    ).value
  )

  def installTask = Def task {
    val lines = "adb devices".lines_!.toSeq
    val device = lines(1).split("\t").head
    val list = Seq(
      "./gradlew --daemon --parallel assembleDebug",
      s"adb -s $device install -r ./linen-starter/build/outputs/apk/linen-starter-debug.apk",
      s"adb -s $device shell am start -n x7c1.linen/x7c1.linen.unread.UnreadItemsActivity"
    )
    list foreach (_ !< streams.value.log)
  }

}
