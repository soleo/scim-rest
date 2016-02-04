package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.data.validation._

object Email {
  
    implicit val emailReads: Reads[Email] = (
        (__ \ "value").readNullable[String](email) and
        (__ \ "type").readNullable[String] and
        (__ \ "primary").readNullable[Boolean]
      )(Email.apply _)

    implicit val emailWrites: Writes[Email] = (
        (__ \ "value").writeNullable[String] and
        (__ \ "type").writeNullable[String] and
        (__ \ "primary").writeNullable[Boolean]
      )(unlift(Email.unapply))

}

case class Email(
    override val value: Option[String] = None,
    override val `type`: Option[String] = None,
    override val primary: Option[Boolean] = None
) extends PluralAttribute
