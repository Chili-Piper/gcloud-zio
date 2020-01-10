
name := "gcloud-zio"

version := "0.1"

scalaVersion := "2.13.1"

lazy val scala213 = "2.13.1"
lazy val scala212 = "2.12.10"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "com.chilipiper"
ThisBuild / scalaVersion := scala213

val publishSettings = Seq(
  bintrayRepository := "gcloud-zio",
  bintrayOrganization := Some("chili-piper"),
  bintrayReleaseOnPublish := true,
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
)


val zioDeps = Seq(
  "dev.zio" %% "zio" % "1.0.0-RC17",
  "dev.zio" %% "zio-streams" % "1.0.0-RC17"
)
val zioReactiveDep = "dev.zio" %% "zio-interop-reactivestreams" % "1.0.3.5-RC2"
val zioJavaDep = "dev.zio" %% "zio-interop-java" % "1.1.0.0-RC6"
val zioFutureDep = "dev.zio" %% "zio-interop-future" % "2.12.8.0-RC6"

lazy val root = (project in file("."))
  .aggregate(pubsub)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val pubsub = (project in file("pubsub"))
  .settings(
    publishSettings,
    crossScalaVersions := supportedScalaVersions,

    libraryDependencies ++= zioDeps,
    libraryDependencies += zioJavaDep,
    libraryDependencies += "com.google.cloud" % "google-cloud-pubsub" % "1.96.0",
  )

