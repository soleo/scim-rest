package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
/**
  * Created by xinjiang on 1/16/16.
  */
object Photo {
  //implicit val photoFormat = Json.format[Photo]
   implicit val photoReads: Reads[Photo] = (
      (__ \ "value").read[String] and
      (__ \ "type").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(Photo.apply _)

  implicit val photoWrites: Writes[Photo] = (
      (__ \ "value").write[String] and
      (__ \ "type").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(Photo.unapply))
}
case class Photo (
                   value: String,
                   photoType: String,
                   primary: Option[Boolean]
                 )