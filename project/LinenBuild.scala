import play.twirl.sbt.SbtTwirl
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.MergeStrategy
import x7c1.wheat.build.WheatSettings.{directories, packages, wheat}
import x7c1.wheat.build.{WheatDirectories, WheatPackages, WheatSettings}

import scala.io.Source

object LinenBuild extends Build with LinenSettings {

  lazy val linenSettings = Seq(
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
    ),
    libraryDependencies += testLibrary,
    logLevel in assembly := Level.Error
  )
  lazy val testLibrary = "org.scalatest" %% "scalatest" % "2.2.4" % Test

  lazy val `wheat-ancient` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath)

  lazy val `linen-glue` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    dependsOn(`wheat-ancient`)

  lazy val `linen-pickle` = project.
    settings(linenSettings:_*).
    settings(
      unmanagedJars in Compile ++= androidSdkClasspath,
      assemblyOutputPath in assembly := pickleJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value
    )

  lazy val `wheat-macros` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    settings(libraryDependencies +=
      "org.scala-lang" % "scala-reflect" % scalaVersion.value )

  lazy val `wheat-modern` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    dependsOn(`wheat-macros`)

  lazy val `wheat-lore` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    dependsOn(`wheat-modern`, `wheat-ancient`)

  lazy val `linen-repository` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    dependsOn(`wheat-modern`)

  lazy val `linen-modern` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := (unmanagedJars in Compile in `linen-pickle`).value).
    settings(libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "org.apache.maven" % "maven-ant-tasks" % "2.1.3" % Test,
      "org.robolectric" % "android-all" % "5.1.1_r9-robolectric-1" % Test,
      "junit" % "junit" % "4.12" % Test,
      "org.robolectric" % "robolectric" % "3.0" % Test
    )).
    settings(

      // not work?
      // javaOptions in (Test, run) += "-Djava.awt.headless=true",

      fork in Test := true
    ).
    settings(
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
      assemblyOutputPath in assembly := linenJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value,
      assemblyMergeStrategy in assembly := discardTargets.value
    ).
    dependsOn(`linen-glue`, `wheat-lore`, `linen-repository`, `linen-pickle`)

  lazy val `wheat-build` = project.
    settings(
      sbtPlugin := true,
      libraryDependencies += testLibrary
    ).
    settings(
      organization := "x7c1",
      name         := "wheat-build",
      version      := "0.1-SNAPSHOT"
    ).
    enablePlugins(SbtTwirl)

  lazy val root = Project("linen", file(".")).
    aggregate(`linen-modern`).
    settings(linenTasks(`linen-modern`):_*).
    settings(WheatSettings.all:_*).
    settings(
      packages in wheat := linenPackages,
      directories in wheat := linenDirectories
    )

}

trait LinenSettings {

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

  lazy val linenJarPath = (assemblyJarName in assembly) map { jar =>
    file("linen-starter") / "libs-generated" / jar
  }

  lazy val pickleJarPath = (assemblyJarName in assembly) map { jar =>
    file("linen-pickle") / "libs-generated" / jar
  }

  lazy val androidJars = (fullClasspath in assembly) map { path =>
    path filter {_.data.getAbsolutePath startsWith androidSdk.getAbsolutePath}
  }

  lazy val androidSdkClasspath = {
    val dirs = {
      (androidSdk / "extras/android/support/v7/appcompat/libs") +++
      (androidSdk / "extras/android/support/v7/recyclerview/libs") +++
      (androidSdk / "extras/android/support/v7/cardview/libs") +++
      (androidSdk / "extras/android/support/design/libs") +++
      (androidSdk / "platforms/android-23")
    }
    (dirs * "*.jar").classpath
  }

  lazy val discardTargets: Def.Initialize[String => MergeStrategy] = {
    val ignore = (path: String) =>
      (path startsWith "org/jdom") || (path startsWith "JDOMAbout") ||
      (path startsWith "com/google/code/rome") || (path startsWith "META-INF") ||
      (path startsWith "x7c1/linen/glue") ||
      (path startsWith "x7c1/wheat/ancient")

    Def.setting {
      case path if ignore(path) => MergeStrategy.discard
      case path =>
        if (!(path startsWith "x7c1")){
          sLog.value info s"may be duplicate between linen-pickle and linen-modern : $path"
        }
        val original = (assemblyMergeStrategy in assembly).value
        original(path)
    }
  }

  lazy val assembleAndInstall =
    Def.taskKey[Unit]("Build android app and install to connected device.")

  def linenTasks(project: Project) = Seq(
    assembleAndInstall := installTask.value,
    assembleAndInstall <<= (assembleAndInstall dependsOn (assembly in project))
  )

  def installTask = Def task {
    val lines = "adb devices".lines_!.toSeq
    val device = lines(1).split("\t").head
    val list = Seq(
      "./gradlew --daemon --parallel assembleDebug",
      s"adb -s $device install -r ./linen-starter/build/outputs/apk/linen-starter-debug.apk",
      s"adb -s $device shell am start -n x7c1.linen/x7c1.linen.unread.UnreadItemsActivity"
    )
    list foreach {_ !< streams.value.log}
  }

  private lazy val androidSdk = {
    val lines = Source.fromFile(file("local.properties")).getLines()
    val regex = "^sdk.dir=(.*)".r
    lines collectFirst { case regex(path) => file(path) } getOrElse {
      throw new IllegalStateException("sdk.dir not found")
    }
  }
}
