package models

import play.api.mvc.{RequestHeader, AnyContent, Request}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._

import java.util.UUID
import java.util.Date

import models.User._
import models._
import models.dao.UserDAO
import models.dao.GroupDAO
import utils._

object User {
    
    implicit val userReadFormat: Reads[User] = new Reads[User] {
        override def reads(json: JsValue): JsResult[User] = {
            for {
               baseUser <- json.validate[BaseUser]
               emails   <- (json \ "emails").validateOpt[List[Email]]
               phoneNumbers <- (json \ "phoneNumbers").validateOpt[List[PhoneNumber]]
               ims <- (json \ "ims").validateOpt[List[Im]]
               photos <- (json \ "photos").validateOpt[List[Photo]]
               addresses <- (json \ "addresses").validateOpt[List[Address]]
               entitlements <- (json \ "entitlements").validateOpt[List[Entitlement]]
               roles <-  (json \ "roles").validateOpt[List[Role]]
               x509Certificates <- (json \ "x509Certificates").validateOpt[List[X509Certificate]]
            }yield{
                User("", baseUser, emails, phoneNumbers, ims, photos, addresses, None, entitlements, roles, x509Certificates )
            }
            
        }
    }
    
    implicit val userWrites = Json.writes[User]
    
    def findOneByEmail(email: String): Option[String] = {
        Some(email)
    }
    
    def authenticate(email: String, password: String) : Boolean = {
        //temp block for auth. should use another source to match password
        if(email == "shaoxinjiang@gmail.com" && password == "123456")
            true
        else
            false
    }
    
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
      val meta: Option[Meta] = Some(Meta(new Date, new Date, None, None))
      UserDAO.create(id, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs, meta)
      User(id, baseUser, emails, phoneNumbers, ims, photos, addresses, groups, entitlements, roles, x509certs, meta )
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
    
    def hasConflicts(userName: String, userId: String = "") : Boolean = {
        val user: User = User(userId, BaseUser(userName))
        if(UserDAO.exists(user)) {
            true
        }else{
            false
        }
    }
    
    def setMetaData(user: User, request: RequestHeader): Meta = {
        val location = Utils.baseURL(request) +  "Users/" + user.id
        val version  = Utils.generateETAG(Json.stringify(Json.toJson(user)))
        
        user.meta match {
            case Some(m) => Meta(m.created, m.lastModified, Some(version), Some(location))
            case None => Meta(new Date, new Date, Some(version), Some(location))
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
  roles: Option[List[Role]] = None,
  x509Certificates: Option[List[X509Certificate]] = None,
  meta: Option[Meta] = None
)
 


