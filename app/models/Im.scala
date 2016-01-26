package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError


object Im {
  //implicit val imFormat = Json.format[Im]
   implicit val imReads: Reads[Im] = (
      (__ \ "value").read[String] and
      (__ \ "type").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(Im.apply _)

  implicit val imWrites: Writes[Im] = (
      (__ \ "value").write[String] and
      (__ \ "type").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(Im.unapply))
}

case class Im (
                value: String,
                imType: String,
                primary: Option[Boolean]
              )