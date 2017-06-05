
name := "namejuggler"
organization := "edu.umass.cs.iesl"
scalaVersion := "2.11.11"

scalacOptions ++= Seq(
  "-deprecation"
    , "-encoding", "UTF-8"
    , "-feature"
    , "-unchecked"
    // , "-language:existentials"
    // , "-language:higherKinds"
    , "-language:implicitConversions"
    , "-language:postfixOps"
    , "-Xlint"
    , "-Ywarn-adapted-args"
    , "-Ywarn-inaccessible"
    , "-Ywarn-unused-import"
    , "-Ywarn-unused"
    , "-Ywarn-dead-code"
    // , "-Ypartial-unification"
    , "-Xfuture"
)


libraryDependencies ++= Seq(
  "org.apache.commons"         % "commons-lang3"     % "3.5",
  "ch.qos.logback"             % "logback-classic"   % "1.1.7",
  "org.scalatest"              %% "scalatest"        % "3.0.3",
  "com.typesafe.scala-logging" %% "scala-logging"    % "3.5.0"

)












