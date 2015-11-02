import play.twirl.sbt.SbtTwirl
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.MergeStrategy
import x7c1.wheat.build.{WheatDirectories, WheatPackages, WheatSettings}

import scala.io.Source

object LinenBuild extends Build with LinenSettings {

  lazy val linenSettings = Seq(
    scalaVersion := "2.11.6",
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
      unmanagedJars in Compile := androidSdkClasspath,
      assemblyOutputPath in assembly := pickleJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value
    )

  lazy val `wheat-modern` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath)

  lazy val `linen-modern` = project.
    settings(linenSettings:_*).
    settings(
      unmanagedJars in Compile := androidSdkClasspath,
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
      assemblyOutputPath in assembly := linenJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value,
      assemblyMergeStrategy in assembly := excludeGlue.value
    ).
    dependsOn(`linen-glue`, `wheat-modern`)

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
    settings(WheatSettings.all:_*).
    settings(
      WheatSettings.packages := linenPackages,
      WheatSettings.directories := linenDirectories
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
      val support = "extras/android/support/v7/appcompat/libs"
      val platform = "platforms/android-23"
      (androidSdk / platform) +++ (androidSdk / support)
    }
    (dirs * "*.jar").classpath
  }

  lazy val excludeGlue: Def.Initialize[String => MergeStrategy] =
    Def.setting {
      case path if path startsWith "x7c1/linen/glue" => MergeStrategy.discard
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
