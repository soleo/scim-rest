package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

import play.api.data.validation._

object Email {
  
  val emailValidation: Reads[String] = Reads.StringReads.filter(ValidationError("Invalid Email given!"))(str => {
        str.matches("""^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""")
  })
  
  implicit val emailReads: Reads[Email] = (
      (__ \ "value").read[String](emailValidation) and
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
