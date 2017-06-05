// import sbt._
// import sbtassembly.Plugin._

// import edu.umass.cs.iesl.sbtbase.{Dependencies, IeslProject}
// import edu.umass.cs.iesl.sbtbase.IeslProject._

// object NameJugglerBuild extends Build
// 	{

// 	val vers = "0.1-SNAPSHOT"

// 	implicit val allDeps = new Dependencies()

// 	import allDeps._

// 	val deps = Seq(ieslScalaCommons("latest.integration"), scalatest(),commonsLang())

// 	lazy val namejuggler = Project("namejuggler",file(".")).ieslSetup(vers, deps, Public, WithSnapshotDependencies)
// 	                      .settings(assemblySettings: _*).cleanLogging.standardLogging
// }
