package models

import play.api.libs.json.Json

object X509Certificate {
  implicit val x509CertificateFormat = Json.format[X509Certificate]
}


case class X509Certificate (
                             value: String,
                             primary: Boolean
                           )
