import com.typesafe.sbt.pgp.PgpSettings.pgpPassphrase
import sbt.Keys._
import sbt._

object Publishing {

  private lazy val credentialSettings = Seq(
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )

  private lazy val pgpPassphraseSettings =
    Option(System.getenv().get("PGP_PASSPHRASE"))
        .map(s => pgpPassphrase := Some(s.toCharArray)).toSeq

  private lazy val sharedSettings: Seq[Def.Setting[_ >: Option[Resolver] with ((MavenRepository) => Boolean) with Boolean]] = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("Snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("Releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )

  private lazy val generalSettings = Seq(
    homepage := Some(url("https://github.com/alexandrnikitin/bloom-filter-scala")),
    licenses := Seq("MIT" -> url("https://github.com/alexandrnikitin/bloom-filter-scala/blob/master/LICENSE")),
    scmInfo := Some(ScmInfo(url("https://github.com/alexandrnikitin/bloom-filter-scala"), "scm:git:git@github.com:alexandrnikitin/bloom-filter-scala.git")),
    developers := List(Developer("AlexandrNikitin", "Alexandr Nikitin", "nikitin.alexandr.a@gmail.com", url("https://github.com/alexandrnikitin/")))
  )

  lazy val settings = generalSettings ++ sharedSettings ++ credentialSettings ++ pgpPassphraseSettings

}
