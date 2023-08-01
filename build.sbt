name := """cooksm.art"""
organization := "art.cooksm"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(lombokized)
  .dependsOn(lombokized)

lazy val lombokized = project

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies += "com.auth0" % "java-jwt" % "3.8.2"
libraryDependencies ++= Seq(javaWs)
libraryDependencies += "io.seruco.encoding" % "base62" % "0.1.3"

libraryDependencies ++= Seq(javaJpa % "test", "org.hibernate" % "hibernate-core" % "5.4.2.Final" % "test")
libraryDependencies += "com.github.database-rider" % "rider-core" % "1.7.2" % "test"
libraryDependencies += "org.mockito" % "mockito-core" % "3.1.0" % "test"
libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.4.0" % "test"
libraryDependencies += "org.hamcrest" % "hamcrest-library" % "1.3" % "test"

javaOptions in Test ++= Seq("-Dconfig.file=conf/application.test.conf")

enablePlugins(JacocoCoverallsPlugin)
jacocoExcludes ++= Seq("controllers.v1.javascript*")
jacocoExcludes ++= Seq("router.*")
jacocoExcludes ++= Seq("data.entities.*")
