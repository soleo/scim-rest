name := """scim-rest"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.34",
  ws,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  specs2 % Test,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
)
libraryDependencies += evolutions
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


routesGenerator := InjectedRoutesGenerator

fork in run := true

herokuAppName in Compile := "pacific-beach-4736"

coverageEnabled := true
