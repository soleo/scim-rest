package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object PhoneNumber {
   implicit val phoneNumberFormat = Json.format[PhoneNumber]
}

case class PhoneNumber(
                        value: String,
                        `type`: String,
                        primary: Option[Boolean] = None
                      )