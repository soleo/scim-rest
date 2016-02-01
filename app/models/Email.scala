package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.data.validation._

object Email extends PluralAttribute {
  
 
  implicit val emailReads: Reads[Email] = (
      (__ \ "value").read[String](email) and
      (__ \ "type").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(Email.apply _)

  implicit val emailWrites: Writes[Email] = (
      (__ \ "value").write[String] and
      (__ \ "type").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(Email.unapply))

}

case class Email(
                  value : String,
                  `type`: String,
                  primary: Option[Boolean] = None
                )
