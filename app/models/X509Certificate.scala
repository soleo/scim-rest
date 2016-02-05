package models

import play.api.libs.json.Json

object X509Certificate {
  implicit val x509CertificateFormat = Json.format[X509Certificate]
}

case class X509Certificate(
  override val value: Option[String] = None,
  override val `type`: Option[String] = None,
  override val primary: Option[Boolean] = None) extends PluralAttribute
