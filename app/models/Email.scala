package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
/**
  * Created by xinjiang on 1/16/16.
  */
object Email {
  //def notEqualReads[T](v: T)(implicit r: Reads[T]): Reads[T] = Reads.filterNot(ValidationError("validate.error.unexpected.value", v))( _ == v )

  implicit val emailReads: Reads[Email] = (
      (__ \ "value").read[String] and
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
                  emailType: String,
                  primary: Option[Boolean]
                )
