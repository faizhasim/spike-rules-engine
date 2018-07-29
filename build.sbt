name := """spike-rules-engine"""
organization := "com.faizhasim.spike.rulesengine"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

lazy val slickVersion = "3.0.1"

libraryDependencies ++= Seq(evolutions, jdbc, filters, ehcache)

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",

  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,


  "org.flywaydb" %% "flyway-play" % "5.0.0",
  "com.typesafe.play" %% "play-slick" % slickVersion,
  "com.typesafe.play" %% "play-slick-evolutions" % slickVersion,
//  "com.h2database" % "h2" % "1.4.197",
  "mysql" % "mysql-connector-java" % "8.0.11"
)

routesGenerator := InjectedRoutesGenerator


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.faizhasim.spike.rulesengine.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.faizhasim.spike.rulesengine.binders._"
