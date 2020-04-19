name := """openrecipes"""
organization := "com.openrecipes"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(lombokized)
  .dependsOn(lombokized)

lazy val lombokized = project

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies ++= Seq(evolutions, jdbc)

libraryDependencies ++= Seq(javaJpa % "test", "org.hibernate" % "hibernate-core" % "5.4.2.Final" % "test")
libraryDependencies += "com.github.database-rider" % "rider-core" % "1.7.2" % "test"

javaOptions in Test ++= Seq("-Dconfig.file=conf/application.test.conf")

enablePlugins(JacocoCoverallsPlugin)
jacocoExcludes ++= Seq("controllers.v1.javascript*")
jacocoExcludes ++= Seq("router.*")
jacocoExcludes ++= Seq("models.entities.*")
