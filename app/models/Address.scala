package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Address {
  implicit val addressFormat = Json.format[Address]
}
case class Address(
                    `type`: String,
                    streetAddress: String,
                    locality: String,
                    region: String,
                    postalCode: String,
                    country: String,
                    `formatted`: String,
                    primary: Option[Boolean] = None
                  )
