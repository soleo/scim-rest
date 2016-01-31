package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.json._
import play.api.db._
import models.Group
import models.Meta

class GroupController extends Controller {

  def findAll = AuthenticatedAction { implicit request =>
    
    val groups = Group.findAll
    var total: Int = 0
    var resources = Json.arr()
    
    for(group <- groups){
        
        val meta = Group.setMetaData(group, request)
        var groupJsonObj = Json.toJson(group).as[JsObject] ++ Json.obj("meta" -> Json.toJson(meta).as[JsObject])
        resources = resources :+ groupJsonObj
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

  def update(groupId : String) = AuthenticatedAction { implicit request =>
    request.body.asJson.map { implicit json =>
      json.validate[Group].map {
        case group =>
        {
            Ok(Json.toJson(group))
        }
      }.recoverTotal{
        e => BadRequest(JsError.toJson(e))
      }
    }.getOrElse {
       BadRequest(Json.obj("error" -> JsString("Expecting Json data")))
    }
  }

}