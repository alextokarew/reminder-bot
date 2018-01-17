organization := "com.github.alextokarew.telegram.bots.reminder"

name := "reminder-bot"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.4"

val akkaVersion = "2.5.9"
val akkaHttpVersion = "10.0.11"

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.1.10" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)