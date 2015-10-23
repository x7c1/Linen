import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.MergeStrategy

import scala.io.Source

object LinenBuild extends Build with LinenSettings {

  val linenSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" % "scalatest_2.11" % "2.2.4" % Test
    ),
    logLevel in assembly := Level.Error
  )

  lazy val `interfaces` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath)

  lazy val `pickle` = project.
    settings(linenSettings:_*).
    settings(
      unmanagedJars in Compile := androidSdkClasspath,
      assemblyOutputPath in assembly := pickleJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value
    )

  lazy val `modern` = project.
    settings(linenSettings:_*).
    settings(
      unmanagedJars in Compile := androidSdkClasspath,
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
      assemblyOutputPath in assembly := linenJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value,
      assemblyMergeStrategy in assembly := excludeInterfaces.value
    ).
    dependsOn(`interfaces`)

  lazy val root = Project("linen", file(".")).aggregate(`modern`)
}

trait LinenSettings {

  lazy val linenJarPath = (assemblyJarName in assembly) map { jar =>
    file("starter") / "libs" / jar
  }

  lazy val pickleJarPath = (assemblyJarName in assembly) map { jar =>
    file("pickle") / "libs" / jar
  }

  lazy val androidJars = (fullClasspath in assembly) map { path =>
    path filter {_.data.getAbsolutePath startsWith androidSdk.getAbsolutePath}
  }

  lazy val androidSdkClasspath = {
    val dirs = {
      val support = "extras/android/support/v7/appcompat/libs"
      val platform = "platforms/android-23"
      (androidSdk / platform) +++ (androidSdk / support)
    }
    (dirs * "*.jar").classpath
  }

  lazy val excludeInterfaces: Def.Initialize[String => MergeStrategy] =
    Def.setting {
      case path if path contains "/linen/interfaces" => MergeStrategy.discard
      case path =>
        val original = (assemblyMergeStrategy in assembly).value
        original(path)
    }

  private lazy val androidSdk = {
    val lines = Source.fromFile(file("local.properties")).getLines()
    val regex = "^sdk.dir=(.*)".r
    lines collectFirst { case regex(path) => file(path) } getOrElse {
      throw new IllegalStateException("sdk.dir not found")
    }
  }
}
