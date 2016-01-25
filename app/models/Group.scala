package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Group {
  implicit val groupFormat = Json.format[Group]
}


case class Group (
                 display: String,
                 value: String
                 )