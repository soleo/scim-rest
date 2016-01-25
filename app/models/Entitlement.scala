package models


import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError

/**
  * Created by xinjiang on 1/16/16.
  */
object Entitlement {
 
   implicit val entitlementReads: Reads[Entitlement] = (
      (__ \ "value").read[String] and
      (__ \ "type").read[String] and
      (__ \ "primary").readNullable[Boolean]
    )(Entitlement.apply _)

  implicit val entitlementWrites: Writes[Entitlement] = (
      (__ \ "value").write[String] and
      (__ \ "type").write[String] and
      (__ \ "primary").writeNullable[Boolean]
    )(unlift(Entitlement.unapply))
}

case class Entitlement (
                         value: String,
                         entitlementType: String,
                         primary: Option[Boolean]
                       )