package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError


object Im extends PluralAttribute {
   implicit val imFormat = Json.format[Im]

}

case class Im (
                value: String,
                `type`: String,
               primary: Option[Boolean] = None
              )