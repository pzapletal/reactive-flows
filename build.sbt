val reactiveFlows = project
  .in(file("."))
  .configs(MultiJvm)
  .enablePlugins(AutomateHeaderPlugin, GitVersioning, JavaAppPackaging, DockerPlugin)

organization := "de.heikoseeberger"
name         := "reactive-flows"
licenses     += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion   := "2.11.7"
scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

unmanagedSourceDirectories.in(Compile)  := List(scalaSource.in(Compile).value)
unmanagedSourceDirectories.in(Test)     := List(scalaSource.in(Test).value)
unmanagedSourceDirectories.in(MultiJvm) := List(scalaSource.in(MultiJvm).value)

val akkaVersion       = "2.4.0"
val akkaHttpVersion   = "1.0"
libraryDependencies ++= List(
  "com.github.krasserm"      %% "akka-persistence-cassandra"         % "0.4",
  "com.typesafe.akka"        %% "akka-cluster-sharding"              % akkaVersion,
  "com.typesafe.akka"        %% "akka-distributed-data-experimental" % akkaVersion,
  "com.typesafe.akka"        %% "akka-http-experimental"             % akkaHttpVersion,
  "com.typesafe.akka"        %% "akka-http-spray-json-experimental"  % akkaHttpVersion,
  "de.heikoseeberger"        %% "akka-log4j"                         % "1.0.1",
  "de.heikoseeberger"        %% "akka-macro-logging"                 % "0.1.0",
  "de.heikoseeberger"        %% "akka-sse"                           % "1.1.0",
  "de.heikoseeberger"        %% "constructr"                         % "0.2.0",
  "org.apache.logging.log4j" %  "log4j-core"                         % "2.3",
  "com.typesafe.akka"        %% "akka-http-testkit-experimental"     % akkaHttpVersion % "test",
  "com.typesafe.akka"        %% "akka-multi-node-testkit"            % akkaVersion     % "test",
  "com.typesafe.akka"        %% "akka-testkit"                       % akkaVersion     % "test",
  "org.scalatest"            %% "scalatest"                          % "2.2.5"         % "test"
)

initialCommands := """|import de.heikoseeberger.reactiveflows._""".stripMargin

git.baseVersion := "2.1.0"

import scalariform.formatter.preferences._
preferences := preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
inConfig(MultiJvm)(SbtScalariform.configScalariformSettings)
inConfig(MultiJvm)(compileInputs.in(compile) := { format.value; compileInputs.in(compile).value })

headers := Map("scala" -> de.heikoseeberger.sbtheader.license.Apache2_0("2015", "Heiko Seeberger"))
AutomateHeaderPlugin.automateFor(Compile, Test, MultiJvm)
HeaderPlugin.settingsFor(Compile, Test, MultiJvm)

test.in(Test)         := { scalastyle.in(Compile).toTask("").value; test.in(Test).value }
scalastyleFailOnError := true

coverageMinimum          := 100
coverageFailOnMinimum    := true
coverageExcludedPackages := ".*App"

maintainer in Docker := "Heiko Seeberger"
version in Docker    := "latest"
daemonUser in Docker := "root"
dockerRepository     := Some("hseeberger")

addCommandAlias("rf1", "reStart -Dreactive-flows.http-service.port=8001 -Dakka.remote.netty.tcp.port=2551 -Dcassandra-journal.contact-points.0=192.168.99.100")
addCommandAlias("rf2", "run     -Dreactive-flows.http-service.port=8002 -Dakka.remote.netty.tcp.port=2552 -Dcassandra-journal.contact-points.0=192.168.99.100")
addCommandAlias("rf3", "run     -Dreactive-flows.http-service.port=8003 -Dakka.remote.netty.tcp.port=2553 -Dcassandra-journal.contact-points.0=192.168.99.100")
