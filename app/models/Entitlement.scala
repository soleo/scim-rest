package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Entitlement {
    implicit val entitlementFormat = Json.format[Entitlement]
}

case class Entitlement (
    override val value: Option[String] = None,
    override val `type`: Option[String] = None,
    override val primary: Option[Boolean] = None
) extends PluralAttribute