package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

object Photo {
  implicit val photoFormat = Json.format[Photo]
}
case class Photo (
                   value: String,
                   `type`: String,
                   primary: Option[Boolean] = None
                 )