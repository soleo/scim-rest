package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

object Name {
    implicit val nameFormat = Json.format[Name]
}

case class Name (
                  formatted:       Option[String],
                  familyName:      Option[String],
                  givenName:       Option[String],
                  middleName:      Option[String],
                  honorificPrefix: Option[String],
                  honorificSuffix: Option[String]
                )
