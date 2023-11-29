scalaVersion := "2.13.8"

name := "FashionDigitalAssignment"
organization := "org.ilimturan"
version := "1.0.0"

libraryDependencies ++= {
  val akkaV         = "2.6.18"
  val akkaHttpV     = "10.2.7"
  val quillV        = "3.5.3"
  val scalaLoggingV = "3.9.2"
  val logbackV      = "1.2.3"
  val scalaTestV    = "3.0.8"
  val mockitoV      = "3.4.6"
  val postgresqlV   = "42.1.4"
  val commonsIoV              = "2.6"
  val commonsValidatorV       = "1.6"

  Seq(
    "com.typesafe.akka"          %% "akka-actor"           % akkaV,
    "com.typesafe.akka"          %% "akka-stream"          % akkaV,
    "com.typesafe.akka"          %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"          %% "akka-http-spray-json" % akkaHttpV,
    "io.getquill"                %% "quill-async-postgres" % quillV,
    "org.postgresql"              % "postgresql"           % postgresqlV,
    "com.typesafe.scala-logging" %% "scala-logging"        % scalaLoggingV,
    "ch.qos.logback"              % "logback-classic"      % logbackV,
    "commons-io"                     % "commons-io"                         % commonsIoV,
    "commons-validator"              % "commons-validator"                  % commonsValidatorV exclude ("commons-logging", "commons-logging"),
    "org.mockito"                 % "mockito-core"         % mockitoV   % Test,
    "org.scalatest"              %% "scalatest"            % scalaTestV % Test,
    "com.typesafe.akka"          %% "akka-stream-testkit"  % akkaV      % Test,
    "com.typesafe.akka"          %% "akka-http-testkit"    % akkaHttpV  % Test
  )
}

assembly / assemblyMergeStrategy := {
  case PathList("producer.conf")     => MergeStrategy.concat
  case PathList("consumer.conf")     => MergeStrategy.concat
  case PathList("rest.conf")         => MergeStrategy.concat
  case PathList("postgres.conf")     => MergeStrategy.concat
  case PathList("reference.conf")    => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}

//assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
assembly / assemblyJarName := s"fashion_digital_assignment.jar"

coverageMinimum := 80
coverageFailOnMinimum := true
coverageExcludedPackages := "org.ilimturan.config.*;org.ilimturan.Boot"
