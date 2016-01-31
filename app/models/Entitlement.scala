package models


import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Entitlement {
   implicit val entitlementFormat = Json.format[Entitlement]
  
}

case class Entitlement (
                         value: String,
                         `type`: String,
                         primary: Option[Boolean] = None
                       )