name := """scim-rest"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.18",
  ws,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  specs2 % Test,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
)
libraryDependencies += evolutions

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


routesGenerator := InjectedRoutesGenerator

fork in run := true

herokuAppName in Compile := "pacific-beach-4736"