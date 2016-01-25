package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by xinjiang on 1/16/16.
  */


object Name {
  implicit val nameReads: Reads[Name] = (
      (__ \ "formatted").readNullable[String] and
      (__ \ "familyName").readNullable[String] and
      (__ \ "givenName").readNullable[String] and
      (__ \ "middleName").readNullable[String] and
      (__ \ "honorificPrefix").readNullable[String] and
      (__ \ "honorificSuffix").readNullable[String]
    )(Name.apply _)

  implicit val nameWrites: Writes[Name] = (
      (__ \ "formatted").writeNullable[String] and
      (__ \ "familyName").writeNullable[String] and
      (__ \ "givenName").writeNullable[String] and
      (__ \ "middleName").writeNullable[String] and
      (__ \ "honorificPrefix").writeNullable[String] and
      (__ \ "honorificSuffix").writeNullable[String]
    )(unlift(Name.unapply))
}

case class Name (
                  formattedName:   Option[String],
                  familyName:      Option[String],
                  givenName:       Option[String],
                  middleName:      Option[String],
                  honorificPrefix: Option[String],
                  honorificSuffix: Option[String]
                )
