package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.json._
import play.api.db._
import models.Group
import models.Meta

object GroupController extends Controller {

  def findAll = Action { implicit request =>
    
    val groups = Group.findAll
    var total: Int = 0
    var resources = Json.arr()
    
    for(group <- groups) {
        //group.meta = RequestUtils.addMetaData("Groups", group.id, group.meta, request)
        var groupJsonObj = Json.toJson(group) 
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

  def update(groupId : String) = Action { implicit request =>
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
      BadRequest("Expecting Json data")
    }
  }

}