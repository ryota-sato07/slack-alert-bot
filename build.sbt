import Dependencies._

ThisBuild / scalaVersion     := "2.12.17"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "slack-notification-bot",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.9.0",

      "com.amazonaws" % "aws-java-sdk" % "1.12.99",

      "software.amazon.awssdk" % "costexplorer"     % "2.17.96",
      "com.slack.api"          % "slack-api-client" % "1.17.0"
    )
  )

