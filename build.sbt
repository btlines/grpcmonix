organization in ThisBuild := "beyondthelines"
version in ThisBuild := "0.0.5-SNAPSHOT"
bintrayOrganization in ThisBuild := Some(organization.value)
bintrayRepository in ThisBuild := "maven"
bintrayPackageLabels in ThisBuild := Seq("scala", "protobuf", "grpc", "monix")
licenses in ThisBuild := ("MIT", url("http://opensource.org/licenses/MIT")) :: Nil

scalaVersion in ThisBuild := "2.12.4"

lazy val runtime = (project in file("runtime"))
  .settings(
    crossScalaVersions := Seq("2.12.4", "2.11.11"),
    name := "GrpcMonixRuntime",
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % "0.6.7",
      "io.monix"               %% "monix"                % "2.3.0"
    )
  )

lazy val generator = (project in file("generator"))
  .settings(
    crossScalaVersions := Seq("2.12.4", "2.10.6"),
    name := "GrpcMonixGenerator",
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.7",
      "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % "0.6.7"
    )
  )
