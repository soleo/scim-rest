package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

/**
  * Created by xinjiang on 1/16/16.
  */
object Address {
  //implicit val addressFormat = Json.format[Address]
  implicit val emailReads: Reads[Address] = (
      (__ \ "type").read[String] and
      (__ \ "streetAddress").read[String] and
      (__ \ "locality").read[String] and
      (__ \ "region").read[String] and
      (__ \ "postalCode").read[String] and
      (__ \ "country").read[String] and
      (__ \ "formatted").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(Address.apply _)

  implicit val emailWrites: Writes[Address] = (
      (__ \ "type").write[String] and
      (__ \ "streetAddress").write[String] and
      (__ \ "locality").write[String] and
      (__ \ "region").write[String] and
      (__ \ "postalCode").write[String] and
      (__ \ "country").write[String] and
      (__ \ "formatted").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(Address.unapply))
}
case class Address(
                    addressType: String,
                    streetAddress: String,
                    locality: String,
                    region: String,
                    postalCode: String,
                    country: String,
                    formatted: String,
                    primary: Option[Boolean]
                  )
