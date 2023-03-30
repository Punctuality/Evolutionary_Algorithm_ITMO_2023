ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0-RC3"

lazy val lab6 = (project in file("."))
  .settings(
    name := "Lab_6",
    scalacOptions ++= Seq(
        "-feature",
    )
  )

libraryDependencies ++= Seq(
  // watchmaker framework with excluded dependency
  "org.uncommons.watchmaker" % "watchmaker-framework" % "0.7.1" exclude("com.google.collections", "google-collections"),
  "com.google.guava" % "guava" % "31.1-jre",
  // cats
   "org.typelevel" %% "cats-effect" % "3.4.8",
  //  fs2
   "co.fs2" %% "fs2-core" % "3.6.1",
   "co.fs2" %% "fs2-io" % "3.6.1",
)