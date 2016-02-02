package controllers

import java.util.Date
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.db._
import play.api.libs.json._
import utils._
import models._
import models.User._


class UserController extends Controller {

  def findAll(filter: Option[String]) = AuthenticatedAction { implicit request =>
    val users = User.findAll(filter)
    var total: Int = 0
    var resources = Json.arr()
    
    for(userObj <- users) {
        val meta = User.setMetaData(userObj, request)
        userObj.baseUser.password = None  
        var groups = User.addGroupInfo(userObj)
        var response = User.removeBaseTraits(Json.toJson(userObj)).as[JsObject] ++ Json.toJson(userObj.baseUser).as[JsObject]
            response = response ++ Json.toJson(groups).as[JsObject]
            response = response ++ Json.obj("meta" -> Json.toJson(meta).as[JsObject])
        // append a user to the resource list
        resources = resources :+ response
        // Increase Count Now
        total += 1
    }
            
    Ok(
        Json.obj(
            "totalResults" -> total,
            "schemas" -> Json.arr("urn:scim:schemas:core:1.0"),
            "Resources" -> resources
        )
    )
    
  }
  
  def find(userId : String) = AuthenticatedAction { implicit request =>
    
    val user = User.findOne(userId)
    user match {
        case None => NotFound(Utils.resourceNotFoundMessage(userId))
        case Some(userObj) => {
            val meta = User.setMetaData(userObj, request)
            //reset password before converting to JSON
            userObj.baseUser.password = None  
            var groups = User.addGroupInfo(userObj)
            var response = User.removeBaseTraits(Json.toJson(userObj)).as[JsObject] ++ Json.toJson(userObj.baseUser).as[JsObject]
            response = response ++ Json.toJson(groups).as[JsObject]
            response = response ++ Json.obj("meta" -> Json.toJson(meta).as[JsObject])
            // post fetching 
            val ETag = meta.version.getOrElse("")
            val location = meta.location.getOrElse("")
            Ok(response).withHeaders(
                LOCATION  -> location,
                ETAG      -> ETag
            )
        } 
    }
    
  }
  
  // RAW JSON -> Transformed JSON -> O-O -> DB Store -> O-O -> JSON -> Meta, Group Info Added Json
  def add = AuthenticatedAction { implicit request =>
    request.body.asJson.map { implicit json => 
        // validate User
        json.validate[User] match {
            case u: JsSuccess[user] => {
                val user = u.get
                val conflicted = User.hasConflicts(user.baseUser.userName)
                // pre insertion checking
                println(user.baseUser.userName)
                if(conflicted) {
                    Conflict(Json.obj("error" -> JsString("userName conflicts with exsiting user")))
                        
                }else{
                    // normal flow
                    val userObj = User.add(user.baseUser, user.emails, user.phoneNumbers, user.ims, user.photos, user.addresses, 
                               None, /* Ignore Group*/ user.entitlements, user.roles, user.x509Certificates)
                    val meta = User.setMetaData(userObj, request)
                    //reset password before converting to JSON
                    userObj.baseUser.password = None  
                    var groups = User.addGroupInfo(userObj)
                    var response = User.removeBaseTraits(Json.toJson(userObj)).as[JsObject] ++ Json.toJson(userObj.baseUser).as[JsObject]
                    response = response ++ Json.toJson(groups).as[JsObject]
                    response = response ++ Json.obj("meta" -> Json.toJson(meta).as[JsObject])
                    // post insertion 
                    val ETag = meta.version.getOrElse("")
                    val location = meta.location.getOrElse("")
                    Created(response).withHeaders(
                         LOCATION  -> location,
                         ETAG      -> ETag
                    )
                }
                
            }
            case e: JsError => {
                BadRequest(JsError.toJson(e))
            }
        }
    }.getOrElse {
      BadRequest(Json.obj("error" -> JsString("Expecting Json data")))
    }
  }

  def replace(userId : String) = AuthenticatedAction { implicit  request =>
    val user = User.exists(userId)
    user match {
        case None => NotFound(Utils.resourceNotFoundMessage(userId))
        case Some(user) => {
            request.body.asJson.map { implicit json =>
                json.validate[User] match {
                    case u: JsSuccess[user] => {
                        val user = u.get
                        
                        val conflicted = User.hasConflicts(user.baseUser.userName, userId)
                        // pre insertion checking
                        
                        if(conflicted) {
                            Conflict(Json.obj("error" -> JsString("userName conflicts with exsiting user")))
                        }else{
                        
                            val userObj = User.replace(userId, user.baseUser, user.emails, user.phoneNumbers, user.ims, user.photos, user.addresses, 
                                       None, /* Ignore Group*/ user.entitlements, user.roles, user.x509Certificates)
                            val meta = User.setMetaData(userObj, request)
                            //reset password before converting to JSON
                            userObj.baseUser.password = None  
                            var groups = User.addGroupInfo(userObj)
                            var response = User.removeBaseTraits(Json.toJson(userObj)).as[JsObject] ++ Json.toJson(userObj.baseUser).as[JsObject]
                            response = response ++ Json.toJson(groups).as[JsObject]
                            response = response ++ Json.obj("meta" -> Json.toJson(meta).as[JsObject])
                            // post insertion 
                            val ETag = meta.version.getOrElse("")
                            val location = meta.location.getOrElse("")
                            Ok(response).withHeaders(
                                 LOCATION  -> location,
                                 ETAG      -> ETag
                            )
                        }
                        
                    }
                    case e: JsError => {
                        BadRequest(JsError.toJson(e))
                    }
                }
            }.getOrElse {
              BadRequest(Json.obj("error" -> JsString("Expecting Json data")))
            }
        }
    }
  }

  def remove(userId : String) = AuthenticatedAction { implicit request =>
    val user = User.exists(userId)
    user match {
        case None => NotFound(Utils.resourceNotFoundMessage(userId))
        case Some(userObj) => {
          val meta = User.setMetaData(userObj, request)
          
          User.delete(userId)
          
          val ETag = meta.version.getOrElse("")
          val location = meta.location.getOrElse("")
          Ok("").withHeaders(
                     LOCATION  -> location,
                     ETAG      -> ETag
          )
        }
    }
  }


}