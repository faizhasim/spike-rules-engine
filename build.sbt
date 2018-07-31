name := """spike-rules-engine"""
organization := "com.faizhasim.spike.rulesengine"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

lazy val playSlickVersion = "3.0.1"
lazy val slickVersion = "3.2.3"

libraryDependencies ++= Seq(evolutions, jdbc, filters, ehcache)

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",

  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,


  "com.typesafe.play" %% "play-slick" % playSlickVersion,
  "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion,
  "com.typesafe.slick" %% "slick-testkit" % slickVersion % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "ch.qos.logback" % "logback-classic" % "0.9.28" % Test,
  "org.mockito" % "mockito-core" % "2.20.1" % Test,
  "com.h2database" % "h2" % "1.4.197" % Test,

  "mysql" % "mysql-connector-java" % "8.0.11"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-s", "-a")

parallelExecution in Test := false

logBuffered := false

routesGenerator := InjectedRoutesGenerator


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.faizhasim.spike.rulesengine.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.faizhasim.spike.rulesengine.binders._"
