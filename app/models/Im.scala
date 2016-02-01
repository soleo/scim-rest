package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError


object Im {
   implicit val imFormat = Json.format[Im]

}

case class Im (    
    override val value: String,
    override val `type`: String,
    override val primary: Option[Boolean] = None
) extends PluralAttribute