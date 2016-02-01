package models

import play.api.libs.json.Json

object X509Certificate extends PluralAttribute {
  implicit val x509CertificateFormat = Json.format[X509Certificate]
}


case class X509Certificate (
                             value: String,
                             `type`: String,
                             primary: Option[Boolean] = None
                           )
