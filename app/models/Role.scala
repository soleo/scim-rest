package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Role extends PluralAttribute {
   implicit val roleFormat = Json.format[Role]
}

case class Role(
                 value: String,
                 `type`: String,
                 primary: Option[Boolean] = None
               )