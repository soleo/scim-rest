package models


import java.util.Date
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Writes._
/**
  * Created by xinjiang on 1/16/16.
  */
object Meta {
  implicit val metaReads = Json.reads[Meta]

  implicit val metaWrites: Writes[Meta] = (
    (__ \ "created").write(dateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")) and
      (__ \ "lastModified").write(dateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")) and
      (__ \ "version").writeNullable[String] and
      (__ \ "location").writeNullable[String] and
      (__ \ "attributes").writeNullable[String]
    )(unlift(Meta.unapply))

}

case class Meta (
                  created: Date,
                  lastModified: Date,
                  version: Option[String] = None,
                  location: Option[String] = None,
                  attributes: Option[String] = None
                )