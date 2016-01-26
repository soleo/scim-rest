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
    val filter = request.queryString.get("filter")
    //println(filter)
    //@TODO: Parse filter syntax
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
          val response = Json.toJson(user)
          val flattenedJson = User.removeBaseTraits(response)
         
          // combine results
          val jsonObject = flattenedJson.as[JsObject]
          val userGroups = User.addGroupInfo(user)// readOnly
          val finalResponse = jsonObject ++ Json.toJson(user.baseUser).as[JsObject] ++ userGroups
         
          Ok(finalResponse)
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
          // @TODO: check for confilicts before adding, return 409 if conficts with current Users eg userName 
          
          val fullUser: User = User.add(
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
        
          fullUser.meta = RequestUtils.addMetaData("Users", fullUser.id, fullUser.meta,  request)
          
          //reset password before converting to JSON
          fullUser.baseUser.password = None 
          val response = Json.toJson(fullUser)
          val flattenedJson = User.removeBaseTraits(response)
          
          // combine results
          val jsonObject = flattenedJson.as[JsObject]
          val finalResponse = jsonObject ++ Json.toJson(fullUser.baseUser).as[JsObject]

          Created(finalResponse)
            
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
                    println(baseUser)
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
                    val fullUser: User = User.replace(
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
                    fullUser.meta = RequestUtils.addMetaData("Users", fullUser.id, fullUser.meta, request)
          
                    //reset password before converting to JSON
                    fullUser.baseUser.password = None 
                    
                    val response = Json.toJson(fullUser)
                    val flattenedJson = User.removeBaseTraits(response)
                    // combine results
                    val jsonObject = flattenedJson.as[JsObject]
                    val userGroups = User.addGroupInfo(fullUser)// readOnly
                    val finalResponse = jsonObject ++ Json.toJson(fullUser.baseUser).as[JsObject] ++ userGroups
                    
                    Ok(finalResponse)
                  
                    
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
          User.delete(userId)
          Ok("")
        }
    }
  }

  // https://gist.github.com/guillaumebort/2328236 for auth

}