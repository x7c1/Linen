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

  lazy val `wheat-macros` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    settings(libraryDependencies +=
      "org.scala-lang" % "scala-reflect" % scalaVersion.value )

  lazy val `wheat-modern` = project.
    settings(linenSettings:_*).
    settings(unmanagedJars in Compile := androidSdkClasspath).
    dependsOn(`wheat-macros`)

  lazy val `linen-modern` = project.
    settings(linenSettings:_*).
    settings(
      unmanagedJars in Compile := androidSdkClasspath,
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
      assemblyOutputPath in assembly := linenJarPath.value,
      assemblyExcludedJars in assembly := androidJars.value,
      assemblyMergeStrategy in assembly := discardGradleTargets.value
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
      val appcompat = "extras/android/support/v7/appcompat/libs"
      val recycler = "extras/android/support/v7/recyclerview/libs"
      val platform = "platforms/android-23"
      (androidSdk / platform) +++ (androidSdk / appcompat) +++ (androidSdk / recycler)
    }
    (dirs * "*.jar").classpath
  }

  lazy val discardGradleTargets: Def.Initialize[String => MergeStrategy] = {
    val forGradle = (path: String) =>
      (path startsWith "x7c1/linen/glue") ||
      (path startsWith "x7c1/wheat/ancient")

    Def.setting {
      case path if forGradle(path) => MergeStrategy.discard
      case path =>
        val original = (assemblyMergeStrategy in assembly).value
        original(path)
    }
  }

  private lazy val androidSdk = {
    val lines = Source.fromFile(file("local.properties")).getLines()
    val regex = "^sdk.dir=(.*)".r
    lines collectFirst { case regex(path) => file(path) } getOrElse {
      throw new IllegalStateException("sdk.dir not found")
    }
  }
}
