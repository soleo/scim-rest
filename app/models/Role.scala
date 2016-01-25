package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

/**
  * Created by xinjiang on 1/16/16.
  */
object Role {
  //implicit val roleFormat = Json.format[Role]
   implicit val roleReads: Reads[Role] = (
      (__ \ "value").read[String] and
      (__ \ "type").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(Role.apply _)

  implicit val roleWrites: Writes[Role] = (
      (__ \ "value").write[String] and
      (__ \ "type").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(Role.unapply))
}

case class Role(
                 value: String,
                 Roletype: String,
                 primary: Option[Boolean]
               )