organization := "com.edmondcote"

name := "romfilter"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
  "org.scala-lang.modules" %% "scala-swing" % "2.0.3"
)

fork := true // needed by scala-xml, workaround earlier pre-1.1.2 versions of sbt
