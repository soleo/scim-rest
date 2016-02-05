package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Address {
  implicit val addressFormat = Json.format[Address]
}

case class Address(
  `type`: Option[String] = None,
  streetAddress: Option[String] = None,
  locality: Option[String] = None,
  region: Option[String] = None,
  postalCode: Option[String] = None,
  country: Option[String] = None,
  `formatted`: Option[String] = None,
  primary: Option[Boolean] = None)

