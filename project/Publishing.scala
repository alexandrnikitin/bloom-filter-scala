import sbt._
import sbt.Keys._
import xerial.sbt.Sonatype.SonatypeKeys._

object Publishing {

  private lazy val credentialSettings = Seq(
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
    
    credentials += Credentials(
      "GnuPG Key ID",
      "gpg",
      "nikitin.alexandr.a@gmail.com", // key identifier
      "ignored" // this field is ignored; passwords are supplied by pinentry
    )
  )

  private lazy val sharedSettings = Seq(
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := Function.const(false),
    publishTo := sonatypePublishToBundle.value,
    sonatypeSessionName := "[sbt-sonatype] ${name.value}-${scalaBinaryVersion.value}-${version.value}"
  )

  private lazy val generalSettings = Seq(
    homepage := Some(url("https://github.com/alexandrnikitin/bloom-filter-scala")),
    licenses := Seq("MIT" -> url("https://github.com/alexandrnikitin/bloom-filter-scala/blob/master/LICENSE")),
    scmInfo := Some(ScmInfo(url("https://github.com/alexandrnikitin/bloom-filter-scala"), "scm:git:git@github.com:alexandrnikitin/bloom-filter-scala.git")),
    developers := List(Developer("AlexandrNikitin", "Alexandr Nikitin", "nikitin.alexandr.a@gmail.com", url("https://github.com/alexandrnikitin/")))
  )

  lazy val settings = generalSettings ++ sharedSettings ++ credentialSettings

  lazy val noPublishSettings = Seq(
    publish / skip := true
  )

}
