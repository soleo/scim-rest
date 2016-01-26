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

class UserController extends Controller {

  def findAll = Action { implicit request =>
    val filter = request.queryString.get("filter")
    println(request.queryString.toList)
    //@TODO: Parse filter syntax
    // Only support or, and, co, eq for emails field to do filter
    val users = User.findAll(filter)
    var total: Int = 0
    var resources = Json.arr()
    
    for(user <- users) {
        user.meta = RequestUtils.addMetaData("Users", user.id, user.meta,  request)
        val singleUser = Json.toJson(user)
        val flattenedJson = User.removeBaseTraits(singleUser)
        val jsonObject = flattenedJson.as[JsObject]
        val userGroups = User.addGroupInfo(user)// readOnly
        var finalUser = jsonObject ++ Json.toJson(user.baseUser).as[JsObject] ++ userGroups
        
        resources = resources :+ finalUser
        // Increase Count Now
        total += 1
    }
            
    Ok(
        Json.obj(
            "totalResults" -> total,
            "schemes" -> Json.arr("urn:scim:schemas:core:1.0"),
            "Resources" -> resources
        )
    )
    
  }
  
  def find(userId : String) = Action { implicit request =>
    
    val user = User.findOne(userId)
    user match {
        case None => NotFound(RequestUtils.notFoundMessage(userId))
        case Some(user) => {
          
          user.meta = RequestUtils.addMetaData("Users", user.id, user.meta, request)
          val location = RequestUtils.getLocation(user.meta)
          val response = Json.toJson(user)
          val flattenedJson = User.removeBaseTraits(response)
         
          // combine results
          val jsonObject = flattenedJson.as[JsObject]
          val userGroups = User.addGroupInfo(user)// readOnly
          val finalResponse = jsonObject ++ Json.toJson(user.baseUser).as[JsObject] ++ userGroups
          val ETag = RequestUtils.generateETAG(user.meta.getOrElse(Meta(new Date, new Date)))
          Ok(finalResponse).withHeaders(
                "Location" -> location,
                ETAG       -> ETag.getOrElse("")
            )
        } 
    }
    
  }
  
  // RAW JSON -> Transformed JSON -> Case Class -> DB Store -> Case Class -> JSON -> Meta Added
  def add = Action { implicit request =>
    request.body.asJson.map { implicit json =>
      json.validate[BaseUser].map {
        case baseUser => {
          
          // @TODO: add validation later
          val emails: Option[List[Email]] = (json \ "emails").asOpt[List[Email]]
          val phoneNumbers: Option[List[PhoneNumber]] = (json \ "phoneNumbers").asOpt[List[PhoneNumber]]
          val ims: Option[List[Im]] = (json \ "ims").asOpt[List[Im]]
          val photos: Option[List[Photo]] = (json \ "photos").asOpt[List[Photo]]
          val addresses: Option[List[Address]] = (json \ "addresses").asOpt[List[Address]]
          //val groups: Option[List[Group]] = (json \ "groups").asOpt[List[Group]]
          val entitlements: Option[List[Entitlement]] = (json \ "entitlements").asOpt[List[Entitlement]]
          val roles: Option[List[Role]] = (json \ "roles").asOpt[List[Role]]
          val x509certs: Option[List[X509Certificate]] = (json \ "x509Certificates").asOpt[List[X509Certificate]]
          // @TODO: check for confilicts before adding, return 409 if conficts with current Users e.g userName 
          val status = User.checkConflicts(baseUser.userName)
          if(!status) {
              Conflict("userName Exists Already")
          }
          val user: User = User.add(
                                        baseUser, 
                                        emails, 
                                        phoneNumbers,
                                        ims,
                                        photos,
                                        addresses,
                                        None, // Group
                                        entitlements,
                                        roles,
                                        x509certs
                                        )
        
          user.meta = RequestUtils.addMetaData("Users", user.id, user.meta, request)
          
          //reset password before converting to JSON
          user.baseUser.password = None 
          val response = Json.toJson(user)
          val flattenedJson = User.removeBaseTraits(response)
          
          // combine results
          val jsonObject = flattenedJson.as[JsObject]
          val finalResponse = jsonObject ++ Json.toJson(user.baseUser).as[JsObject]
          val location = RequestUtils.getLocation(user.meta)
          val ETag = RequestUtils.generateETAG(user.meta.getOrElse(Meta(new Date, new Date)))
          Created(finalResponse).withHeaders(
                "Location" -> location,
                 ETAG      -> ETag.getOrElse("")
            )
            
        }
      }.recoverTotal{
        e => BadRequest(JsError.toJson(e))
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def replace(userId : String) = Action { implicit  request =>
    val user = User.exists(userId)
    user match {
        case None => NotFound(RequestUtils.notFoundMessage(userId))
        case Some(user) => {
            request.body.asJson.map { implicit json =>
              json.validate[BaseUser].map {
                case baseUser => {
                    //println(baseUser)
                    val emails: Option[List[Email]] = (json \ "emails").asOpt[List[Email]]
                    val phoneNumbers: Option[List[PhoneNumber]] = (json \ "phoneNumbers").asOpt[List[PhoneNumber]]
                    val ims: Option[List[Im]] = (json \ "ims").asOpt[List[Im]]
                    val photos: Option[List[Photo]] = (json \ "photos").asOpt[List[Photo]]
                    val addresses: Option[List[Address]] = (json \ "addresses").asOpt[List[Address]]
                    // groups are readOnly
                    //val groups: Option[List[Group]] = (json \ "groups").asOpt[List[Group]]
                    val entitlements: Option[List[Entitlement]] = (json \ "entitlements").asOpt[List[Entitlement]]
                    val roles: Option[List[Role]] = (json \ "roles").asOpt[List[Role]]
                    val x509certs: Option[List[X509Certificate]] = (json \ "x509Certificates").asOpt[List[X509Certificate]]
                    // ignore any read-only attributes and password
                    val user: User = User.replace(
                                        userId,
                                        baseUser, 
                                        emails, 
                                        phoneNumbers,
                                        ims,
                                        photos,
                                        addresses,
                                        None,
                                        entitlements,
                                        roles,
                                        x509certs
                                        )
                    user.meta = RequestUtils.addMetaData("Users", user.id, user.meta, request)
          
                    //reset password before converting to JSON
                    user.baseUser.password = None 
                    
                    val response = Json.toJson(user)
                    val flattenedJson = User.removeBaseTraits(response)
                    // combine results
                    val jsonObject = flattenedJson.as[JsObject]
                    val userGroups = User.addGroupInfo(user)// readOnly
                    val finalResponse = jsonObject ++ Json.toJson(user.baseUser).as[JsObject] ++ userGroups
                    
                    val ETag = RequestUtils.generateETAG(user.meta.getOrElse(Meta(new Date, new Date)))
                    val location = RequestUtils.getLocation(user.meta)
                    
                    Ok(finalResponse).withHeaders(
                            "Location" -> location,
                            ETAG -> ETag.getOrElse("")
                    )
                    
                  
                    
                }
              }.recoverTotal{
                e => BadRequest(JsError.toJson(e))
              }
            }.getOrElse {
              BadRequest("Expecting Json data")
            }
        }
    }
  }

  def remove(userId : String) = Action { implicit request =>
    val user = User.exists(userId)
    user match {
        case None => NotFound(RequestUtils.notFoundMessage(userId))
        case Some(user) => {
          val meta = RequestUtils.addMetaData("Users", user.id, user.meta, request)
          val location = RequestUtils.getLocation(meta)
          val ETag = RequestUtils.generateETAG(meta.getOrElse(Meta(new Date, new Date)))
          
          User.delete(userId)
          
          Ok("").withHeaders(
                "Location" -> location,
                 ETAG      -> ETag.getOrElse("")
            )
          
        }
    }
  }

  // https://gist.github.com/guillaumebort/2328236 for auth

}