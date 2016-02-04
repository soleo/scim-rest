package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Writes._
import java.util.Date

object Meta {

  //implicit val metaReads = Json.reads[Meta]
  // totally ignore other fileds, becasue they should be ready only
  implicit val metaReadFormat: Reads[Meta] = new Reads[Meta] {
        override def reads(json: JsValue): JsResult[Meta] = {
            for {
                attributes  <- (json \ "attributes").validateOpt[List[String]]
            }yield{
                Meta(new Date, new Date, None, None, attributes )
            }
        }
  }
    
  implicit val metaWrites: Writes[Meta] = (
    (__ \ "created").write(dateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")) and
      (__ \ "lastModified").write(dateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")) and
      (__ \ "version").writeNullable[String] and
      (__ \ "location").writeNullable[String] and
      (__ \ "attributes").writeNullable[List[String]]
    )(unlift(Meta.unapply))

}

case class Meta (
  created: Date,
  lastModified: Date,
  version: Option[String] = None,
  location: Option[String] = None,
  attributes: Option[List[String]] = None
)