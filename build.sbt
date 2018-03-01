import scalapb.compiler.Version.scalapbVersion

organization in ThisBuild := "beyondthelines"
version in ThisBuild := "0.0.6"
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
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion,
      "io.monix"             %% "monix"                % "2.3.3"
    )
  )

lazy val generator = (project in file("generator"))
  .settings(
    crossScalaVersions := Seq("2.12.4", "2.10.6"),
    name := "GrpcMonixGenerator",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin"       % scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion
    )
  )

