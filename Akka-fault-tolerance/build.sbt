name := "untitled"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= {
  val akkaVersion = "2.6.8"
  Seq(
    "com.typesafe.akka"       %%  "akka-actor-typed"               % akkaVersion,
    "com.typesafe.akka"       %%  "akka-slf4j"                     % akkaVersion,
    "com.typesafe.akka"       %%  "akka-testkit"                   % akkaVersion   % "test",
    "org.scalatest"           %% "scalatest"                       % "3.2.9"       % "test"
  )
}
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.25"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime