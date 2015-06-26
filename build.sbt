import spray.revolver.RevolverPlugin._

name := "hyperdev"

organization := "org.hyperscala"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.6"

seq(Revolver.settings: _*)

libraryDependencies += "org.hyperscala" %% "hyperscala-ui" % "0.10.1-SNAPSHOT"

libraryDependencies += "com.outr.net" %% "outrnet-jetty" % "1.1.5-SNAPSHOT"

assemblyJarName in assembly := "hyperdev.jar"

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter(af => Set("annotations-2.0.1.jar").contains(af.data.getName))
}