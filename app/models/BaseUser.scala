package models

import models.User._
import models._
import models.dao.UserDAO
//import models.Group
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._

import java.util.UUID

object BaseUser {

    // implicit val userReads: Reads[User] = (
    //     (__ \ "schemas").read[List[String]] and
    //     (__ \ "id").readNullable[String] and
    //     (__ \ "userName").readNullable[String] and
    //     (__ \ "externalId").readNullable[String] and
    //     (__ \ "name").readNullable[Name] and 
    //     (__ \ "basicTraits").readNullable[BasicTrait] and
    //     (__ \ "emails").readNullable[Seq[Email]] and
    //     (__ \ "phoneNumbers").readNullable[Seq[PhoneNumber]] and
    //     (__ \ "ims").readNullable[Seq[Im]] and
    //     (__ \ "photos").readNullable[Seq[Photo]] and
    //     (__ \ "addresses").readNullable[Seq[Address]] and
    //     (__ \ "groups").readNullable[Seq[Group]] and
    //     (__ \ "entitlements").readNullable[Seq[Entitlement]] and
    //     (__ \ "roles").readNullable[Seq[Role]] and
    //     (__ \ "x509Certificates").readNullable[Seq[X509Certificate]] and
    //     (__ \ "meta").readNullable[Meta]
    //   )(User.apply _)

    implicit val baseUserWrites  = Json.writes[BaseUser]
    implicit val baseUserReads  = Json.reads[BaseUser]
    // implicit val userWrites: Writes[User] = (
    //     (__ \ "schemas").write[List[String]] and
    //     (__ \ "id").writeNullable[String] and
    //     (__ \ "userName").writeNullable[String] and
    //     (__ \ "externalId").writeNullable[String] and
    //     (__ \ "name").writeNullable[Name] and 
    //     (__ \ "basicTraits").writeNullable[BasicTrait] and
        
    //   )(unlift(User.unapply))
    
}

case class BaseUser(
                 
                 userName: String,
                 externalId: Option[String] = None,
                 name: Option[Name] = None,
                 displayName: Option[String] = None,
                 nickName: Option[String] = None,
                 profileUrl: Option[String] = None,
                 title: Option[String] = None,
                 userType: Option[String] = None,
                 preferredLanguage: Option[String] = None,
                 locale: Option[String] = None,
                 timezone: Option[String] = None,
                 active: Option[Boolean] = None,
                 var password: Option[String] = None,
                 schemas: List[String] = List("urn:scim:schemas:core:1.0")
                 //,
                 
                //  var basicTraits: Option[BasicTrait] = None,
                //  emails: Option[Seq[Email]] = None,
                //  phoneNumbers: Option[Seq[PhoneNumber]] = None,
                //  ims: Option[Seq[Im]] = None ,
                //  photos: Option[Seq[Photo]] = None,
                //  addresses: Option[Seq[Address]] = None,
                //  groups: Option[Seq[Group]] = None,
                //  entitlements: Option[Seq[Entitlement]] = None,
                //  roles: Option[Seq[Role]] = None,
                //  x509Certificates: Option[Seq[X509Certificate]] = None,
                // var meta: Option[Meta] = None
               )