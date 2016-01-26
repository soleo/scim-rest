name := """scim-rest"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "mysql" % "mysql-connector-java" % "5.1.18",
  ws,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  specs2 % Test
)
libraryDependencies += evolutions

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
routesGenerator := InjectedRoutesGenerator

fork in run := true