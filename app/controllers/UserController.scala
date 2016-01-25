package controllers

import java.util.Date
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.db._
import play.api.libs.json._
import utils.RequestUtils
import models._
import models.User._


object UserController extends Controller {

  def findAll = Action { implicit request =>
    val filter = request.query.filter
    val users = User.find(filter)
    users match {
        case None => NotFound(Json.obj("error" -> "NOT_FOUND"))
        case Some(users) => {
            
            var resources = JsObject()
            
            for(user <- users) {
                var singleUser = Json.toJson(user)
                var flattenedJson = User.removeBaseTraits(singleUser)
                val jsonObject = flattenedJson.as[JsObject]
                resources += JsObject(jsonObject ++ Json.toJson(user.baseUser).as[JsObject])
            }
            
            Ok(resources)
        }
    }
  }
  
  def find(userId : String) = Action { implicit request =>
    
    val user = User.findOne(userId)
    user match {
        case None => NotFound(Json.obj("error" -> "NOT_FOUND"))
        case Some(user) => {
            val meta: Meta = user.meta match {
            case Some(m) => {
              val location = RequestUtils.baseURL+"Users/" + user.id
              val version = RequestUtils.ETag
              Meta(m.created, m.lastModified, version, Some(location))
            }
            case None => {
              val location = RequestUtils.baseURL+"Users/" + user.id
              val version = RequestUtils.ETag
              Meta(new Date, new Date, version, Some(location))
            }
          }
          
          user.meta = Some(meta)
          val response = Json.toJson(user)
          val flattenedJson = User.removeBaseTraits(response)
          
          // combine results
          val jsonObject = flattenedJson.as[JsObject]
          val finalResponse = jsonObject ++ Json.toJson(user.baseUser).as[JsObject]

          Ok(finalResponse)
          
        }
    }
    
  }
  // RAW JSON -> Transformed JSON -> Case Class -> DB Store -> Case Class -> JSON -> Meta Added
  def add = Action { implicit request =>
    request.body.asJson.map { implicit json =>
      json.validate[BaseUser].map {
        case baseUser => {
          //println(baseUser)
          val emails: Option[List[Email]] = (json \ "emails").asOpt[List[Email]]
          val phoneNumbers: Option[List[PhoneNumber]] = (json \ "phoneNumbers").asOpt[List[PhoneNumber]]
          val ims: Option[List[Im]] = (json \ "ims").asOpt[List[Im]]
          val photos: Option[List[Photo]] = (json \ "photos").asOpt[List[Photo]]
          val addresses: Option[List[Address]] = (json \ "addresses").asOpt[List[Address]]
          val groups: Option[List[Group]] = (json \ "groups").asOpt[List[Group]]
          val entitlements: Option[List[Entitlement]] = (json \ "entitlements").asOpt[List[Entitlement]]
          val roles: Option[List[Role]] = (json \ "roles").asOpt[List[Role]]
          val x509certs: Option[List[X509Certificate]] = (json \ "x509Certificates").asOpt[List[X509Certificate]]
         
          val fullUser: User = User.add(
                                        baseUser, 
                                        emails, 
                                        phoneNumbers,
                                        ims,
                                        photos,
                                        addresses,
                                        groups,
                                        entitlements,
                                        roles,
                                        x509certs
                                        )
          val meta: Meta = fullUser.meta match {
            case Some(m) => {
              val location = RequestUtils.baseURL+"Users/" + fullUser.id
              val version = RequestUtils.ETag
              Meta(m.created, m.lastModified, version, Some(location))
            }
            case None => {
              val location = RequestUtils.baseURL+"Users/" + fullUser.id
              val version = RequestUtils.ETag
              Meta(new Date, new Date, version, Some(location))
            }
          }
          
          fullUser.meta = Some(meta)
          //reset password before converting to JSON
          fullUser.baseUser.password = None 
          val response = Json.toJson(fullUser)
          val flattenedJson = User.removeBaseTraits(response)
          
          // combine results
          val jsonObject = flattenedJson.as[JsObject]
          val finalResponse = jsonObject ++ Json.toJson(fullUser.baseUser).as[JsObject]

          Ok(finalResponse)

        }
      }.recoverTotal{
        e => BadRequest(JsError.toJson(e))
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def replace(userId : String) = Action { implicit  request =>
    Ok("")
  }

  def remove(userId : String) = Action { implicit request =>
    Ok("") 
  }

  // https://gist.github.com/guillaumebort/2328236

}