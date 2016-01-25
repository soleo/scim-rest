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

object User {
    
    implicit val userFormat  = Json.format[User]
    
    def add(baseUser: BaseUser, 
            emails: Option[List[Email]], 
            phoneNumbers: Option[List[PhoneNumber]],
            ims: Option[List[Im]],
            photos: Option[List[Photo]],
            addresses: Option[List[Address]],
            groups: Option[List[Group]],
            entitlements: Option[List[Entitlement]],
            roles: Option[List[Role]],
            x509certs: Option[List[X509Certificate]]
            ): User  =  {
      val id: String = UUID.randomUUID.toString
     
      UserDAO.create(id, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs)
      User(id, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs )
    }
    
    def delete(userId: String) = {
      UserDAO.delete(User(userId))
    }

    def replace(userId: String,
            baseUser: BaseUser, 
            emails: Option[List[Email]], 
            phoneNumbers: Option[List[PhoneNumber]],
            ims: Option[List[Im]],
            photos: Option[List[Photo]],
            addresses: Option[List[Address]],
            groups: Option[List[Group]],
            entitlements: Option[List[Entitlement]],
            roles: Option[List[Role]],
            x509certs: Option[List[X509Certificate]]
      ) = {

      UserDAO.updateOne(User(userId, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs))
    }

    def findOne(userId: String): Option[User] = {
      val user = User(userId)
      
      if(UserDAO.exists(user)) {
          val u:List[User] = UserDAO.findOne(user)
          Some(u.head)
      }else{
          None
      }
    }
    
    def findAll(filter: Option[Seq[String]]): List[User] = {
        val u:List[User] = UserDAO.findAll(filter)
        u
    }
    
    def removeBaseTraits(userJson: JsValue): JsObject = {
     
      val jsonTransformer = 
      (__ ).json.pickBranch(
          (__ \ 'baseUser).json.prune
      ) 
       
       
      val transformedUserJson = userJson.transform(jsonTransformer)
      transformedUserJson match {
        case JsError(_) => Json.obj()
        case _ => transformedUserJson.get
      }
    }
}    
case class User(
        id: String = "",
        baseUser: BaseUser = BaseUser(""),
        emails: Option[List[Email]] = None,
        phoneNumbers: Option[List[PhoneNumber]] = None,
        ims: Option[List[Im]] = None,
        photos: Option[List[Photo]] = None,
        addresses: Option[List[Address]] = None,
        groups: Option[List[Group]] = None,
        entitlements: Option[List[Entitlement]] = None,
        roles: Option[Seq[Role]] = None,
        x509Certificates: Option[Seq[X509Certificate]] = None,
        var meta: Option[Meta] = None
    )
 


