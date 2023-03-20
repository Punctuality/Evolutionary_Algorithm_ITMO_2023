ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0-RC3"

lazy val root = (project in file("."))
  .settings(
    name := "Lab_5",
//    compiler arguments
    scalacOptions ++= Seq(
        "-feature",
        "-explain"
    )
  )

libraryDependencies ++= Seq(
  // watchmaker framework with excluded dependency
  "org.uncommons.watchmaker" % "watchmaker-framework" % "0.7.1" exclude("com.google.collections", "google-collections"),
  "com.google.guava" % "guava" % "31.1-jre",
  // cats
   "org.typelevel" %% "cats-effect" % "3.4.8"
)