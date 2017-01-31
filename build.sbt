import LinenSettings.{discardTargets, linenDirectories, linenPackages, linenSettings, linenTasks}
import sbtassembly.AssemblyKeys.{assemblyJarName, assemblyMergeStrategy, assemblyOutputPath}
import x7c1.wheat.harvest.WheatSettings
import x7c1.wheat.harvest.WheatSettings.{directories, packages, wheat}
import x7c1.wheat.splicer.assembly.AssemblySettings

lazy val `android-jars` = project.settings(AssemblySettings.forProvider(
  assemblyDirectory = _.base / "libs-assembled",
  splicerDirectory = _.base / "libs-expanded",
  localProperties = file("local.properties"),
  buildGradle = file("build.gradle"),
  dependenciesGradle = file("targets.gradle")
))

lazy val `wheat-ancient` = project.
  settings(linenSettings: _*).
  settings(compileOrder := CompileOrder.JavaThenScala).
  settings(AssemblySettings.forClient(`android-jars`))

lazy val `linen-glue` = project.
  settings(linenSettings: _*).
  dependsOn(`wheat-ancient`)

lazy val `linen-pickle` = project.
  settings(linenSettings: _*).
  settings(
    resolvers += "Android ROME Feed Reader Repository" at
      "https://android-rome-feed-reader.googlecode.com/svn/maven2/releases"
  ).
  settings(libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.9.2",
    "com.typesafe" % "config" % "1.2.1",
    "com.google.code.android-rome-feed-reader" % "android-rome-feed-reader" % "1.0.0-r2"
  )).
  settings(
    assemblyOutputPath in assembly := {
      thisProject.value.base / "libs-generated" / (assemblyJarName in assembly).value
    }
  )

lazy val `wheat-macros` = project.
  settings(linenSettings: _*).
  settings(AssemblySettings.forClient(`android-jars`)).
  settings(libraryDependencies +=
    "org.scala-lang" % "scala-reflect" % scalaVersion.value)

lazy val `wheat-macros-sample` = project.
  settings(linenSettings: _*).
  settings(libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )).
  dependsOn(`wheat-macros`)

lazy val `wheat-modern` = project.
  settings(linenSettings: _*).
  dependsOn(`wheat-macros`, `wheat-calendar`)

lazy val `wheat-lore` = project.
  settings(linenSettings: _*).
  dependsOn(`wheat-modern`, `wheat-ancient`)

lazy val `wheat-calendar` = project.
  settings(linenSettings: _*)

lazy val `linen-repository` = project.
  settings(linenSettings: _*).
  settings(unmanagedJars in Compile ++= {
    (assemblyOutputPath in assembly in `linen-pickle`).value.get.classpath
  }).
  settings(libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.11" % Test,
    "org.apache.maven" % "maven-ant-tasks" % "2.1.3" % Test,
    "org.robolectric" % "android-all" % "5.1.1_r9-robolectric-1" % Test,
    "junit" % "junit" % "4.12" % Test,
    "org.robolectric" % "robolectric" % "3.0" % Test
  )).
  settings(
    javaOptions in Test += "-Djava.awt.headless=true",
    fork in Test := true
  ).
  dependsOn(`wheat-modern`)

lazy val `linen-scene` = project.
  settings(linenSettings: _*).
  dependsOn(`linen-repository`, `linen-glue`, `wheat-lore`)

lazy val `linen-modern` = project.
  settings(linenSettings: _*).
  settings(
    assemblyOutputPath in assembly := {
      file("linen-starter") / "libs-generated" / (assemblyJarName in assembly).value
    },
    assemblyExcludedJars in assembly ++= {
      (assemblyOutputPath in assembly in `linen-pickle`).value.get.classpath
    },
    assemblyMergeStrategy in assembly := discardTargets.value
  ).
  settings(AssemblySettings.forClient(`android-jars`)).
  dependsOn(`linen-scene`)

lazy val root = Project("linen", file(".")).
  aggregate(`linen-modern`).
  settings(linenTasks(`linen-modern`): _*).
  settings(WheatSettings.all: _*).
  settings(
    packages in wheat := linenPackages,
    directories in wheat := linenDirectories
  )
