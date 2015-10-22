import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys.{assembly, assemblyJarName, assemblyOutputPath, assemblyExcludedJars}

import scala.io.Source

object LinenBuild extends Build {

  val linenSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" % "scalatest_2.11" % "2.2.4" % Test
    )
  )

  lazy val `interfaces` = project.
    settings(sdkClasspath).
    settings(linenSettings:_*)

  lazy val `modern` = project.
    settings(sdkClasspath).
    settings(linenSettings:_*).
    settings(linenJarPath).
    settings(assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value

//      cp filter {_.data.getName == "android.jar"}
      cp filter {x => println(x); x.data.getName.contains("android")}
    }).
    dependsOn(`interfaces`)

  lazy val root = Project("linen", file(".")).
    aggregate(`modern`)

  lazy val linenJarPath = assemblyOutputPath in assembly := {
    val jar = (assemblyJarName in assembly).value
    file("starter") / "libs" / jar
  }

  lazy val sdkClasspath = unmanagedJars in Compile := {
    val sdk = {
      val lines = Source.fromFile(file("local.properties")).getLines()
      val regex = "^sdk.dir=(.*)".r
      lines collectFirst { case regex(path) => file(path) } getOrElse {
        throw new IllegalStateException("sdk.dir not found")
      }
    }
    val dirs = {
      val support = "extras/android/support/v7/appcompat/libs"
      val platform = "platforms/android-23"
      (sdk / platform) +++ (sdk / support)
    }
    (dirs * "*.jar").classpath
  }
}
