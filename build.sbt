lazy val root = (project in file(".")).
  settings(
    name := "git-submitter-plugin",
    organization := "org.cs1331",
    version := "0.7-SNAPSHOT",
    sbtPlugin := true,
    scalaVersion := "2.10.4",
    publishMavenStyle := true,
    publishTo := Some(Resolver.sftp("CS 1331 Repository",
      "repo.tweedbathrobe.com", "repo.tweedbathrobe.com/")),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "junit" % "junit" % "4.12",
      "com.google.code.gson" % "gson" % "1.7.1",
      "commons-cli" % "commons-cli" % "1.3.1"
    )
  )
