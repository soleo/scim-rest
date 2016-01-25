package models

import play.api.libs.json.Json

/**
  * Created by xinjiang on 1/16/16.
  */
object X509Certificate {
  implicit val x509CertificateFormat = Json.format[X509Certificate]
}


case class X509Certificate (
                             value: String,
                             primary: Boolean
                           )
