name := """receptnekem"""
organization := "hu.receptnekem"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8"
libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies += "com.auth0" % "java-jwt" % "3.8.2"
libraryDependencies ++= Seq(javaWs)

libraryDependencies ++= Seq(javaJpa % "test", "org.hibernate" % "hibernate-core" % "5.4.2.Final" % "test")
libraryDependencies += "com.github.database-rider" % "rider-core" % "1.7.2" % "test"
libraryDependencies += "org.mockito" % "mockito-core" % "3.1.0" % "test"

javaOptions in Test ++= Seq("-Dconfig.file=conf/application.test.conf")

enablePlugins(JacocoCoverallsPlugin)
jacocoExcludes ++= Seq("controllers.v1.javascript*")
jacocoExcludes ++= Seq("router.*")
jacocoExcludes ++= Seq("models.entities.*")
