package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

object Name {
    implicit val nameFormat = Json.format[Name]
}

case class Name (
    formatted:       Option[String] = None,
    familyName:      Option[String] = None,
    givenName:       Option[String] = None,
    middleName:      Option[String] = None,
    honorificPrefix: Option[String] = None,
    honorificSuffix: Option[String] = None
)
