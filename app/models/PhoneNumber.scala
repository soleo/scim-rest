package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
/**
  * Created by xinjiang on 1/16/16.
  */
object PhoneNumber {
  //implicit val phoneNumberFormat = Json.format[PhoneNumber]
   implicit val phoneNumberReads: Reads[PhoneNumber] = (
      (__ \ "value").read[String] and
      (__ \ "type").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(PhoneNumber.apply _)

  implicit val phoneNumberWrites: Writes[PhoneNumber] = (
      (__ \ "value").write[String] and
      (__ \ "type").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(PhoneNumber.unapply))
}

case class PhoneNumber(
                        value: String,
                        phoneType: String,
                        primary: Option[Boolean]
                      )