
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

val dontPublishSettings = Seq(
  publish := { },
  bintrayReleaseOnPublish := false,
)

val zioDeps = Seq(
  "dev.zio" %% "zio" % "1.0.0-RC17",
  "dev.zio" %% "zio-streams" % "1.0.0-RC17"
)
val zioReactiveDep = "dev.zio" %% "zio-interop-reactivestreams" % "1.0.3.5-RC2"
val zioJavaDep = "dev.zio" %% "zio-interop-java" % "1.1.0.0-RC6"
val zioFutureDep = "dev.zio" %% "zio-interop-future" % "2.12.8.0-RC6"

lazy val root = (project in file("."))
  .aggregate(pubsub, scheduler)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val common = Project("gcloud-zio-common", file("common"))
  .settings(
    dontPublishSettings,
    crossScalaVersions := supportedScalaVersions,

    libraryDependencies ++= zioDeps,
    libraryDependencies += zioJavaDep,
  )

lazy val pubsub = Project("gcloud-zio-pubsub", file("pubsub"))
  .settings(
    publishSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.google.cloud" % "google-cloud-pubsub" % "1.96.0",
  ).dependsOn(common)

lazy val scheduler = Project("gcloud-zio-scheduler", file("scheduler"))
  .settings(
    publishSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.google.cloud" % "google-cloud-scheduler" % "1.22.2",
  ).dependsOn(common)
