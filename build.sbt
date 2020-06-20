
name := "gcloud-zio"

version := "0.1"

scalaVersion := "2.13.2"

lazy val scala213 = "2.13.2"
lazy val scala212 = "2.12.11"
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
  "dev.zio" %% "zio" % "1.0.0-RC21",
  "dev.zio" %% "zio-streams" % "1.0.0-RC21"
)

lazy val root = (project in file("."))
  .aggregate(common, pubsub, scheduler, tasks)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val common = Project("gcloud-zio-common", file("common"))
  .settings(
    publishSettings,
    crossScalaVersions := supportedScalaVersions,

    libraryDependencies ++= zioDeps,
  )

lazy val pubsub = Project("gcloud-zio-pubsub", file("pubsub"))
  .settings(
    publishSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.google.cloud" % "google-cloud-pubsub" % "1.96.0",
  ).dependsOn(common)

lazy val scheduler = Project("gcloud-zio-scheduler", file("scheduler"))
  .settings(
    dontPublishSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.google.cloud" % "google-cloud-scheduler" % "1.22.2",
  ).dependsOn(common)


lazy val tasks = Project("gcloud-zio-tasks", file("tasks"))
  .settings(
    publishSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.google.cloud" % "google-cloud-tasks" % "1.28.2",
  ).dependsOn(common)
