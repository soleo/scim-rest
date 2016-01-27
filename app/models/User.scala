package models

import models.User._
import models._
import models.dao.UserDAO
import models.dao.GroupDAO

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._

import java.util.UUID

object User {
    
    implicit val userFormat  = Json.format[User]
    
    def exists(userId: String) : Option[User] = {
        val user = User(userId)
        if(UserDAO.exists(user)){
            Some(user)
        }else{
            None
        }
    }
    
    def add(
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
            ): User  =  {
      val id: String = UUID.randomUUID.toString
     
      UserDAO.create(id, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs)
      User(id, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs )
    }
    
    def delete(userId: String) = {
      UserDAO.delete(User(userId))
    }

    def replace(
            userId: String,
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
      ): User = {

      UserDAO.updateOne(
                        User(
                             userId, baseUser, emails, 
                             phoneNumbers, ims, photos, addresses, 
                             groups, entitlements, roles, x509certs
                             ),
                        true
                        )
      UserDAO.findOne( User(userId) ).head
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
    
    def findAll(filter: Option[String]): List[User] = {
        UserDAO.findAll(filter)
    }
    
    def addGroupInfo(user: User): JsObject = {
        
        val groups: Option[List[Group]] = GroupDAO.findGroupsByUserId(user.id)

        groups match{
           case Some(grps) => {
              val grpsRemapped = for ( grp <- grps ) 
                                 yield Map( "value" -> grp.id, "display" -> grp.displayName)
             
              val grpsJson = Json.toJson(grpsRemapped)
               Json.obj("groups" -> grpsJson)
           }
           case None => Json.obj()
        }
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
    
    def checkConflicts(userName:String) : Boolean = {
        val user: User = User("", BaseUser(userName))
        if(UserDAO.exists(user)) {
            true
        }else{
            false
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
        var groups: Option[List[Group]] = None,
        entitlements: Option[List[Entitlement]] = None,
        roles: Option[List[Role]] = None,
        x509Certificates: Option[List[X509Certificate]] = None,
        var meta: Option[Meta] = None
    )
 

